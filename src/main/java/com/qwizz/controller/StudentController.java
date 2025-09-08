package com.qwizz.controller;

import com.qwizz.model.*;
import com.qwizz.service.QuizService;
import com.qwizz.service.ResultService;
import com.qwizz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Student-specific controller for quiz taking and result viewing
 */
@Controller
@RequestMapping("/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private ResultService resultService;

    @Autowired
    private UserService userService;

    // Student Dashboard
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal User student, Model model) {
        ResultService.StudentAnalytics analytics = resultService.getStudentAnalytics(student.getId());
        List<Quiz> availableQuizzes = quizService.getPublicQuizzes();
        List<QuizAttempt> recentAttempts = resultService.getUserAttempts(student.getId());
        
        // Get only the 5 most recent attempts
        if (recentAttempts.size() > 5) {
            recentAttempts = recentAttempts.subList(0, 5);
        }
        
        model.addAttribute("student", student);
        model.addAttribute("analytics", analytics);
        model.addAttribute("availableQuizzes", availableQuizzes);
        model.addAttribute("recentAttempts", recentAttempts);
        
        return "student/dashboard";
    }

    // Browse Available Quizzes
    @GetMapping("/quizzes")
    public String browseQuizzes(@RequestParam(required = false) String difficulty,
                               @RequestParam(required = false) String search,
                               @AuthenticationPrincipal User student,
                               Model model) {
        List<Quiz> quizzes;
        
        if (search != null && !search.trim().isEmpty()) {
            quizzes = quizService.searchQuizzes(search);
        } else if (difficulty != null && !difficulty.trim().isEmpty()) {
            try {
                Difficulty diff = Difficulty.valueOf(difficulty.toUpperCase());
                quizzes = quizService.getQuizzesByDifficulty(diff);
            } catch (IllegalArgumentException e) {
                quizzes = quizService.getPublicQuizzes();
            }
        } else {
            quizzes = quizService.getPublicQuizzes();
        }
        
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("selectedDifficulty", difficulty);
        model.addAttribute("searchTerm", search);
        model.addAttribute("student", student);
        
        return "student/browse-quizzes";
    }

    // Quiz Details and Start
    @GetMapping("/quiz/{id}")
    public String viewQuizDetails(@PathVariable Long id, 
                                 @AuthenticationPrincipal User student, 
                                 Model model) {
        Optional<Quiz> quizOpt = quizService.getQuizById(id);
        if (quizOpt.isEmpty() || (!quizOpt.get().isPublic() && !quizOpt.get().getCreatorId().equals(student.getId()))) {
            return "redirect:/student/quizzes?error=notfound";
        }
        
        Quiz quiz = quizOpt.get();
        List<QuizAttempt> previousAttempts = resultService.getUserAttempts(student.getId())
                .stream()
                .filter(attempt -> attempt.getQuizId().equals(id))
                .toList();
        
        // Check if there's an active attempt
        Optional<QuizAttempt> activeAttempt = resultService.getUserAttempts(student.getId())
                .stream()
                .filter(attempt -> attempt.getQuizId().equals(id) && attempt.isInProgress())
                .findFirst();
        
        model.addAttribute("quiz", quiz);
        model.addAttribute("previousAttempts", previousAttempts);
        model.addAttribute("activeAttempt", activeAttempt.orElse(null));
        model.addAttribute("student", student);
        
        return "student/quiz-details";
    }

    // Start Quiz
    @PostMapping("/quiz/{id}/start")
    public String startQuiz(@PathVariable Long id, 
                           @AuthenticationPrincipal User student) {
        Optional<Quiz> quizOpt = quizService.getQuizById(id);
        if (quizOpt.isEmpty() || (!quizOpt.get().isPublic() && !quizOpt.get().getCreatorId().equals(student.getId()))) {
            return "redirect:/student/quizzes?error=notfound";
        }
        
        QuizAttempt attempt = resultService.startQuizAttempt(student.getId(), id);
        return "redirect:/student/quiz/" + id + "/take?attempt=" + attempt.getId();
    }

    // Take Quiz
    @GetMapping("/quiz/{id}/take")
    public String takeQuiz(@PathVariable Long id,
                          @RequestParam Long attempt,
                          @AuthenticationPrincipal User student,
                          Model model) {
        Optional<Quiz> quizOpt = quizService.getQuizById(id);
        if (quizOpt.isEmpty()) {
            return "redirect:/student/quizzes?error=notfound";
        }
        
        Optional<QuizAttempt> attemptOpt = resultService.getUserAttempts(student.getId())
                .stream()
                .filter(a -> a.getId().equals(attempt) && a.getQuizId().equals(id))
                .findFirst();
        
        if (attemptOpt.isEmpty() || attemptOpt.get().isCompleted()) {
            return "redirect:/student/quiz/" + id + "?error=invalidattempt";
        }
        
        Quiz quiz = quizOpt.get();
        List<Question> questions = quizService.getQuestionsByQuizId(id);
        
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        model.addAttribute("attempt", attemptOpt.get());
        model.addAttribute("student", student);
        
        return "student/take-quiz";
    }

    // Submit Quiz
    @PostMapping("/quiz/{id}/submit")
    public String submitQuiz(@PathVariable Long id,
                            @RequestParam Long attempt,
                            @RequestParam Map<String, String> answers,
                            @AuthenticationPrincipal User student) {
        // Convert answer map to the expected format
        Map<Long, String> formattedAnswers = new HashMap<>();
        for (Map.Entry<String, String> entry : answers.entrySet()) {
            if (entry.getKey().startsWith("question_")) {
                try {
                    Long questionId = Long.parseLong(entry.getKey().substring(9));
                    formattedAnswers.put(questionId, entry.getValue());
                } catch (NumberFormatException e) {
                    // Skip invalid question IDs
                }
            }
        }
        
        try {
            QuizAttempt submittedAttempt = resultService.submitQuizAttempt(attempt, formattedAnswers);
            return "redirect:/student/quiz/" + id + "/result?attempt=" + submittedAttempt.getId();
        } catch (RuntimeException e) {
            return "redirect:/student/quiz/" + id + "?error=submit";
        }
    }

    // View Quiz Result
    @GetMapping("/quiz/{id}/result")
    public String viewQuizResult(@PathVariable Long id,
                                @RequestParam Long attempt,
                                @AuthenticationPrincipal User student,
                                Model model) {
        Optional<Quiz> quizOpt = quizService.getQuizById(id);
        if (quizOpt.isEmpty()) {
            return "redirect:/student/quizzes?error=notfound";
        }
        
        Optional<QuizAttempt> attemptOpt = resultService.getUserAttempts(student.getId())
                .stream()
                .filter(a -> a.getId().equals(attempt) && a.getQuizId().equals(id))
                .findFirst();
        
        if (attemptOpt.isEmpty()) {
            return "redirect:/student/quiz/" + id + "?error=invalidattempt";
        }
        
        Quiz quiz = quizOpt.get();
        QuizAttempt quizAttempt = attemptOpt.get();
        List<ResultService.LeaderboardEntry> leaderboard = resultService.getQuizLeaderboard(id, 10);
        
        model.addAttribute("quiz", quiz);
        model.addAttribute("attempt", quizAttempt);
        model.addAttribute("leaderboard", leaderboard);
        model.addAttribute("student", student);
        
        return "student/quiz-result";
    }

    // View All Attempts History
    @GetMapping("/attempts")
    public String viewAttempts(@AuthenticationPrincipal User student, Model model) {
        List<QuizAttempt> attempts = resultService.getUserAttempts(student.getId());
        model.addAttribute("attempts", attempts);
        model.addAttribute("student", student);
        return "student/attempts";
    }

    // View Progress Analytics
    @GetMapping("/progress")
    public String viewProgress(@AuthenticationPrincipal User student, Model model) {
        ResultService.StudentAnalytics analytics = resultService.getStudentAnalytics(student.getId());
        List<QuizAttempt> attempts = resultService.getUserAttempts(student.getId());
        
        model.addAttribute("analytics", analytics);
        model.addAttribute("attempts", attempts);
        model.addAttribute("student", student);
        
        return "student/progress";
    }

    // Quiz Leaderboard
    @GetMapping("/quiz/{id}/leaderboard")
    public String viewLeaderboard(@PathVariable Long id, 
                                 @AuthenticationPrincipal User student, 
                                 Model model) {
        Optional<Quiz> quizOpt = quizService.getQuizById(id);
        if (quizOpt.isEmpty() || (!quizOpt.get().isPublic() && !quizOpt.get().getCreatorId().equals(student.getId()))) {
            return "redirect:/student/quizzes?error=notfound";
        }
        
        Quiz quiz = quizOpt.get();
        List<ResultService.LeaderboardEntry> leaderboard = resultService.getQuizLeaderboard(id, 50);
        
        model.addAttribute("quiz", quiz);
        model.addAttribute("leaderboard", leaderboard);
        model.addAttribute("student", student);
        
        return "student/leaderboard";
    }

    // API Endpoints for AJAX calls
    @GetMapping("/api/quiz/{id}/time-remaining")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTimeRemaining(@PathVariable Long id,
                                                               @RequestParam Long attempt,
                                                               @AuthenticationPrincipal User student) {
        Optional<QuizAttempt> attemptOpt = resultService.getUserAttempts(student.getId())
                .stream()
                .filter(a -> a.getId().equals(attempt) && a.getQuizId().equals(id))
                .findFirst();
        
        if (attemptOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        QuizAttempt quizAttempt = attemptOpt.get();
        Optional<Quiz> quizOpt = quizService.getQuizById(id);
        
        if (quizOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Quiz quiz = quizOpt.get();
        long startTime = quizAttempt.getStartTime().toEpochSecond(java.time.ZoneOffset.UTC);
        long currentTime = java.time.Instant.now().getEpochSecond();
        long timeLimit = quiz.getTimeLimit() * 60; // Convert minutes to seconds
        long timeRemaining = Math.max(0, timeLimit - (currentTime - startTime));
        
        Map<String, Object> response = new HashMap<>();
        response.put("timeRemaining", timeRemaining);
        response.put("timeLimit", timeLimit);
        response.put("isExpired", timeRemaining <= 0);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/quiz/{id}/auto-submit")
    @ResponseBody
    public ResponseEntity<Map<String, String>> autoSubmitQuiz(@PathVariable Long id,
                                                             @RequestParam Long attempt,
                                                             @RequestBody Map<String, String> answers,
                                                             @AuthenticationPrincipal User student) {
        try {
            Map<Long, String> formattedAnswers = new HashMap<>();
            for (Map.Entry<String, String> entry : answers.entrySet()) {
                try {
                    Long questionId = Long.parseLong(entry.getKey());
                    formattedAnswers.put(questionId, entry.getValue());
                } catch (NumberFormatException e) {
                    // Skip invalid question IDs
                }
            }
            
            QuizAttempt submittedAttempt = resultService.submitQuizAttempt(attempt, formattedAnswers);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("redirectUrl", "/student/quiz/" + id + "/result?attempt=" + submittedAttempt.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}
