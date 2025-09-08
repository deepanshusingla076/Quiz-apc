package com.qwizz.controller;

import com.qwizz.model.*;
import com.qwizz.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class DashboardController {
    
    private final UserService userService;
    private final QuizService quizService;
    private final QuizAttemptService quizAttemptService;
    
    public DashboardController(UserService userService, 
                             QuizService quizService,
                             QuizAttemptService quizAttemptService) {
        this.userService = userService;
        this.quizService = quizService;
        this.quizAttemptService = quizAttemptService;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(value = "role", required = false) String role,
                           Authentication authentication, 
                           Model model) {
        // Check if user is authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User user = (User) authentication.getPrincipal();
        
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
        } else {
            prepareStudentDashboardData(model, user);
        }
        
        return "user/dashboard";
    }
    
    private void prepareStudentDashboardData(Model model, User user) {
        // Basic user data
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Student Dashboard - QWIZZ");
        model.addAttribute("userRole", "student");
        
        // Student statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalQuizzesTaken", user.getTotalQuizzesTaken() != null ? user.getTotalQuizzesTaken() : 0);
        stats.put("totalPoints", user.getTotalPoints() != null ? user.getTotalPoints() : 0);
        stats.put("quizStreak", user.getQuizStreak() != null ? user.getQuizStreak() : 0);
        stats.put("averageScore", user.getAverageScore() != null ? user.getAverageScore() : 0.0);
        model.addAttribute("stats", stats);
        
        // Available quizzes for student
        List<Quiz> availableQuizzes = quizService.getPublicQuizzes();
        model.addAttribute("quizzes", availableQuizzes.size() > 10 ? availableQuizzes.subList(0, 10) : availableQuizzes);
        
        // Dashboard statistics
        model.addAttribute("totalQuizzes", availableQuizzes.size());
        model.addAttribute("completedQuizzes", user.getTotalQuizzesTaken() != null ? user.getTotalQuizzesTaken() : 0);
        model.addAttribute("averageScore", user.getAverageScore() != null ? user.getAverageScore().intValue() : 0);
        
        // User rank (simple calculation based on points)
        Integer globalRank = calculateGlobalRank(user);
        model.addAttribute("userRank", globalRank != null ? "#" + globalRank : "#1");
        
        // Recent attempts
        List<QuizAttempt> recentAttempts = quizAttemptService.getRecentAttemptsByUser(user.getId(), 5);
        model.addAttribute("recentAttempts", recentAttempts);
        
        // Activity message
        model.addAttribute("welcomeMessage", "Welcome back, " + user.getFirstName() + "! Ready to take some quizzes?");
    }
    
    private void prepareTeacherDashboardData(Model model, User user) {
        // Basic user data
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Teacher Dashboard - QWIZZ");
        model.addAttribute("userRole", "teacher");
        
        // Teacher-specific data
        List<Quiz> createdQuizzes = quizService.getQuizzesByCreator(user.getId());
        model.addAttribute("recentQuizzes", createdQuizzes.size() > 10 ? createdQuizzes.subList(0, 10) : createdQuizzes);
        
        // Quiz statistics
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalQuizzesCreated", user.getTotalQuizzesCreated() != null ? user.getTotalQuizzesCreated() : 0);
        analytics.put("totalStudentAttempts", calculateTotalAttemptsOnCreatedQuizzes(user.getId()));
        analytics.put("averageQuizScore", calculateAverageQuizRating(user.getId()));
        model.addAttribute("analytics", analytics);
        
        // Recent quiz attempts on teacher's quizzes
        List<QuizAttempt> recentAttemptsOnMyQuizzes = quizAttemptService.getRecentAttemptsByQuizCreator(user.getId(), 10);
        model.addAttribute("recentAttemptsOnMyQuizzes", recentAttemptsOnMyQuizzes);
        
        // Activity message
        model.addAttribute("welcomeMessage", "Welcome back, " + user.getFirstName() + "! Your quizzes are ready for students.");
    }
    
    private Integer calculateGlobalRank(User user) {
        try {
            List<User> topUsers = userService.getTopUsersByPoints();
            for (int i = 0; i < topUsers.size(); i++) {
                if (topUsers.get(i).getId().equals(user.getId())) {
                    return i + 1;
                }
            }
        } catch (Exception e) {
            // If method doesn't exist, return default
            return 1;
        }
        return null; // User not in top rankings
    }
    
    private int calculateTotalAttemptsOnCreatedQuizzes(Long creatorId) {
        List<Quiz> createdQuizzes = quizService.getQuizzesByCreator(creatorId);
        return createdQuizzes.stream()
                .mapToInt(quiz -> quiz.getTotalAttempts() != null ? quiz.getTotalAttempts() : 0)
                .sum();
    }
    
    private double calculateAverageQuizRating(Long creatorId) {
        List<Quiz> createdQuizzes = quizService.getQuizzesByCreator(creatorId);
        if (createdQuizzes.isEmpty()) {
            return 0.0;
        }
        
        double totalRating = createdQuizzes.stream()
                .filter(quiz -> quiz.getAverageScore() != null)
                .mapToDouble(Quiz::getAverageScore)
                .sum();
        
        long ratedQuizzes = createdQuizzes.stream()
                .filter(quiz -> quiz.getAverageScore() != null)
                .count();
        
        return ratedQuizzes > 0 ? totalRating / ratedQuizzes : 0.0;
    }
}
