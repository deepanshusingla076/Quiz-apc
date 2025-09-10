package com.qwizz.controller;

import com.qwizz.model.*;
import com.qwizz.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DashboardController {
    
    private final QuizService quizService;
    private final QuizAttemptService quizAttemptService;
    
    public DashboardController(QuizService quizService,
                             QuizAttemptService quizAttemptService) {
        this.quizService = quizService;
        this.quizAttemptService = quizAttemptService;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(value = "role", required = false) String role,
                           Authentication authentication, 
                           HttpSession session,
                           Model model) {
        // Check session-based authentication first
        User sessionUser = (User) session.getAttribute("user");
        User user = null;
        
        if (sessionUser != null) {
            user = sessionUser;
        } else if (authentication != null && authentication.isAuthenticated()) {
            user = (User) authentication.getPrincipal();
        }
        
        // If no user found, redirect to login
        if (user == null) {
            return "redirect:/login";
        }
        
        // Determine dashboard based on user role or role parameter
        Role userRole = user.getRole();
        if (role != null) {
            try {
                Role requestedRole = Role.valueOf(role.toUpperCase());
                if (requestedRole != userRole) {
                    // If requested role doesn't match user role, redirect to correct dashboard
                    return "redirect:/dashboard?role=" + userRole.name().toLowerCase();
                }
            } catch (IllegalArgumentException e) {
                // Invalid role parameter, redirect based on user role
                return "redirect:/dashboard?role=" + userRole.name().toLowerCase();
            }
        }
        
        // Prepare dashboard data based on user role
        if (userRole == Role.TEACHER) {
            prepareTeacherDashboardData(model, user);
            return "teacher/dashboard";
        } else {
            prepareStudentDashboardData(model, user);
            return "student/dashboard";
        }
    }
    
    private void prepareStudentDashboardData(Model model, User user) {
        // Basic user data
        model.addAttribute("user", user);
        model.addAttribute("student", user);
        model.addAttribute("pageTitle", "Student Dashboard - QWIZZ");
        model.addAttribute("userRole", "student");
        model.addAttribute("currentUser", user);
        
        // Student statistics with enhanced data
        Map<String, Object> stats = new HashMap<>();
        
        // Get user's quiz attempts for accurate statistics
        List<QuizAttempt> userAttempts = quizAttemptService.getAttemptsByUser(user.getId());
        List<QuizAttempt> completedAttempts = userAttempts.stream()
                .filter(QuizAttempt::isCompleted)
                .collect(Collectors.toList());
        
        // Calculate accurate statistics
        int totalQuizzesTaken = completedAttempts.size();
        double averageScore = completedAttempts.stream()
                .mapToDouble(QuizAttempt::getPercentage)
                .average()
                .orElse(0.0);
        int totalPoints = (int) completedAttempts.stream()
                .mapToDouble(attempt -> attempt.getPercentage() * 10) // 10 points per percent
                .sum();
        
        // Quiz streak calculation
        int currentStreak = calculateQuizStreak(completedAttempts);
        
        stats.put("totalQuizzesTaken", totalQuizzesTaken);
        stats.put("totalPoints", totalPoints);
        stats.put("quizStreak", currentStreak);
        stats.put("averageScore", averageScore);
        model.addAttribute("stats", stats);
        
        // Available quizzes for student
        List<Quiz> availableQuizzes = quizService.getPublicQuizzes();
        model.addAttribute("quizzes", availableQuizzes.size() > 10 ? availableQuizzes.subList(0, 10) : availableQuizzes);
        
        // Dashboard statistics
        model.addAttribute("totalQuizzes", availableQuizzes.size());
        model.addAttribute("completedQuizzes", totalQuizzesTaken);
        model.addAttribute("averageScore", (int) Math.round(averageScore));
        
        // User rank calculation
        Integer globalRank = calculateGlobalRank(user, totalPoints);
        model.addAttribute("userRank", globalRank != null ? "#" + globalRank : "#1");
        
        // Recent attempts (last 5)
        List<QuizAttempt> recentAttempts = completedAttempts.stream()
                .sorted((a, b) -> b.getStartTime().compareTo(a.getStartTime()))
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentAttempts", recentAttempts);
        
        // Activity data
        model.addAttribute("welcomeMessage", "Welcome back, " + user.getFirstName() + "! Ready to take some quizzes?");
        model.addAttribute("lastLogin", formatDateTime(LocalDateTime.now()));
        
        // Performance insights
        Map<String, Object> insights = new HashMap<>();
        insights.put("bestSubject", getBestSubject(completedAttempts));
        insights.put("improvementArea", getImprovementArea(completedAttempts));
        insights.put("weeklyProgress", getWeeklyProgress(completedAttempts));
        model.addAttribute("insights", insights);
    }
    
    private void prepareTeacherDashboardData(Model model, User user) {
        // Basic user data
        model.addAttribute("user", user);
        model.addAttribute("teacher", user);
        model.addAttribute("pageTitle", "Teacher Dashboard - QWIZZ");
        model.addAttribute("userRole", "teacher");
        model.addAttribute("currentUser", user);
        
        // Teacher-specific data
        List<Quiz> createdQuizzes = quizService.getQuizzesByCreator(user.getId());
        List<Quiz> recentQuizzes = createdQuizzes.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10)
                .collect(Collectors.toList());
        model.addAttribute("recentQuizzes", recentQuizzes);
        
        // Enhanced analytics
        Map<String, Object> analytics = new HashMap<>();
        
        // Total quizzes created
        int totalQuizzesCreated = createdQuizzes.size();
        
        // Get all attempts on teacher's quizzes
        List<QuizAttempt> allAttemptsOnMyQuizzes = new ArrayList<>();
        for (Quiz quiz : createdQuizzes) {
            List<QuizAttempt> quizAttempts = quizAttemptService.getAttemptsByQuizId(quiz.getId());
            allAttemptsOnMyQuizzes.addAll(quizAttempts);
        }
        
        // Calculate statistics
        int totalStudentAttempts = allAttemptsOnMyQuizzes.size();
        double averageQuizScore = allAttemptsOnMyQuizzes.stream()
                .filter(QuizAttempt::isCompleted)
                .mapToDouble(QuizAttempt::getPercentage)
                .average()
                .orElse(0.0);
        
        // Unique students who took quizzes
        long uniqueStudents = allAttemptsOnMyQuizzes.stream()
                .map(QuizAttempt::getUser)
                .distinct()
                .count();
        
        analytics.put("totalQuizzesCreated", totalQuizzesCreated);
        analytics.put("totalStudentAttempts", totalStudentAttempts);
        analytics.put("averageQuizScore", Math.round(averageQuizScore * 100.0) / 100.0);
        analytics.put("uniqueStudents", uniqueStudents);
        analytics.put("popularQuiz", getMostPopularQuiz(createdQuizzes));
        model.addAttribute("analytics", analytics);
        
        // Recent quiz attempts on teacher's quizzes
        List<QuizAttempt> recentAttemptsOnMyQuizzes = allAttemptsOnMyQuizzes.stream()
                .sorted((a, b) -> b.getStartTime().compareTo(a.getStartTime()))
                .limit(10)
                .collect(Collectors.toList());
        model.addAttribute("recentAttempts", recentAttemptsOnMyQuizzes);
        
        // Quick stats for dashboard cards
        model.addAttribute("totalQuizzes", totalQuizzesCreated);
        model.addAttribute("totalStudents", uniqueStudents);
        model.addAttribute("averageScore", (int) Math.round(averageQuizScore));
        model.addAttribute("totalAttempts", totalStudentAttempts);
        
        // Activity message
        model.addAttribute("welcomeMessage", "Welcome back, " + user.getFirstName() + "! Your quizzes are ready for students.");
        model.addAttribute("lastLogin", formatDateTime(LocalDateTime.now()));
        
        // Teaching insights
        Map<String, Object> insights = new HashMap<>();
        insights.put("mostActiveDay", getMostActiveDay(allAttemptsOnMyQuizzes));
        insights.put("averageCompletionTime", getAverageCompletionTime(allAttemptsOnMyQuizzes));
        insights.put("successRate", getOverallSuccessRate(allAttemptsOnMyQuizzes));
        model.addAttribute("insights", insights);
    }
    
    // Helper methods for enhanced analytics
    
    private int calculateQuizStreak(List<QuizAttempt> attempts) {
        if (attempts.isEmpty()) return 0;
        
        attempts.sort((a, b) -> b.getStartTime().compareTo(a.getStartTime()));
        int streak = 0;
        
        for (QuizAttempt attempt : attempts) {
            if (attempt.getPercentage() >= 70.0) { // Consider 70% as passing
                streak++;
            } else {
                break;
            }
        }
        
        return streak;
    }
    
    private Integer calculateGlobalRank(User user, int totalPoints) {
        // Simple ranking based on points - in real app, you'd query all users
        return 1; // Simplified for now
    }
    
    private String getBestSubject(List<QuizAttempt> attempts) {
        // Simplified - you could analyze quiz categories/subjects
        return "Java Programming";
    }
    
    private String getImprovementArea(List<QuizAttempt> attempts) {
        // Simplified - you could analyze poor-performing categories
        return "Data Structures";
    }
    
    private String getWeeklyProgress(List<QuizAttempt> attempts) {
        long thisWeekAttempts = attempts.stream()
                .filter(attempt -> attempt.getStartTime().isAfter(LocalDateTime.now().minusWeeks(1)))
                .count();
        return thisWeekAttempts > 0 ? "+" + thisWeekAttempts + " quizzes this week" : "No activity this week";
    }
    
    private String getMostPopularQuiz(List<Quiz> quizzes) {
        return quizzes.stream()
                .max(Comparator.comparing(quiz -> quiz.getTotalAttempts() != null ? quiz.getTotalAttempts() : 0))
                .map(Quiz::getTitle)
                .orElse("No quizzes yet");
    }
    
    private String getMostActiveDay(List<QuizAttempt> attempts) {
        Map<String, Long> dayCount = attempts.stream()
                .collect(Collectors.groupingBy(
                    attempt -> attempt.getStartTime().getDayOfWeek().toString(),
                    Collectors.counting()
                ));
        
        return dayCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No data");
    }
    
    private String getAverageCompletionTime(List<QuizAttempt> attempts) {
        double avgTime = attempts.stream()
                .filter(QuizAttempt::isCompleted)
                .mapToDouble(attempt -> attempt.getTimeTaken() != null ? attempt.getTimeTaken() : 0)
                .average()
                .orElse(0.0);
        
        return String.format("%.1f minutes", avgTime / 60.0);
    }
    
    private String getOverallSuccessRate(List<QuizAttempt> attempts) {
        long totalCompleted = attempts.stream().filter(QuizAttempt::isCompleted).count();
        long passed = attempts.stream()
                .filter(QuizAttempt::isCompleted)
                .filter(attempt -> attempt.getPercentage() >= 70.0)
                .count();
        
        if (totalCompleted == 0) return "No data";
        
        double successRate = (double) passed / totalCompleted * 100;
        return String.format("%.1f%%", successRate);
    }
    
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"));
    }
}
