package com.qwizz.controller;

import com.qwizz.model.*;
import com.qwizz.service.QuizService;
import com.qwizz.service.QuizAttemptService;
import com.qwizz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/**
 * Student-specific controller for quiz taking and result viewing
 */
@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuizAttemptService quizAttemptService;

    @Autowired
    private UserService userService;

    // Check if user is logged in and is a student
    private boolean isStudent(HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        String userRole = (String) session.getAttribute("userRole");
        return isLoggedIn != null && isLoggedIn && "STUDENT".equals(userRole);
    }

    @GetMapping("/quiz/{id}")
    public String viewQuizDetails(@PathVariable Long id, Model model, HttpSession session) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }

        Optional<Quiz> quizOpt = quizService.findById(id);
        if (quizOpt.isEmpty()) {
            return "redirect:/dashboard";
        }

        Quiz quiz = quizOpt.get();
        List<Question> questions = quizService.getQuestionsByQuizId(id);
        
        // Check if student has already attempted this quiz
        Long userId = (Long) session.getAttribute("userId");
        List<QuizAttempt> previousAttempts = quizAttemptService.getAttemptsByUserAndQuiz(userId, id);

        model.addAttribute("quiz", quiz);
        model.addAttribute("questionCount", questions.size());
        model.addAttribute("previousAttempts", previousAttempts);
        model.addAttribute("pageTitle", quiz.getTitle() + " - QWIZZ");

        return "student/quiz-details";
    }

    @GetMapping("/quiz/{id}/start")
    public String startQuiz(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }

        Optional<Quiz> quizOpt = quizService.findById(id);
        if (quizOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Quiz not found");
            return "redirect:/dashboard";
        }

        Quiz quiz = quizOpt.get();
        if (!quiz.isActive()) {
            redirectAttributes.addFlashAttribute("errorMessage", "This quiz is not currently available");
            return "redirect:/dashboard";
        }

        Long userId = (Long) session.getAttribute("userId");
        
        try {
            // Create a new quiz attempt
            QuizAttempt attempt = new QuizAttempt();
            attempt.setUserId(userId);
            attempt.setQuizId(id);
            attempt.setStartTime(LocalDateTime.now());
            attempt.setCompleted(false);
            
            QuizAttempt savedAttempt = quizAttemptService.startQuizAttempt(attempt);
            
            return "redirect:/student/quiz/" + id + "/take/" + savedAttempt.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to start quiz: " + e.getMessage());
            return "redirect:/student/quiz/" + id;
        }
    }

    @GetMapping("/quiz/{quizId}/take/{attemptId}")
    public String takeQuiz(@PathVariable Long quizId, 
                          @PathVariable Long attemptId, 
                          Model model, 
                          HttpSession session) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }

        Long userId = (Long) session.getAttribute("userId");
        
        Optional<QuizAttempt> attemptOpt = quizAttemptService.findById(attemptId);
        if (attemptOpt.isEmpty()) {
            return "redirect:/dashboard";
        }

        QuizAttempt attempt = attemptOpt.get();
        
        // Verify that this attempt belongs to the current user
        if (!attempt.getUserId().equals(userId) || !attempt.getQuizId().equals(quizId)) {
            return "redirect:/dashboard";
        }

        // Check if already completed
        if (attempt.isCompleted()) {
            return "redirect:/student/quiz/" + quizId + "/result/" + attemptId;
        }

        Optional<Quiz> quizOpt = quizService.findById(quizId);
        if (quizOpt.isEmpty()) {
            return "redirect:/dashboard";
        }

        Quiz quiz = quizOpt.get();
        List<Question> questions = quizService.getQuestionsByQuizId(quizId);

        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        model.addAttribute("attempt", attempt);
        model.addAttribute("pageTitle", "Taking: " + quiz.getTitle());

        return "student/quiz-take";
    }

    @PostMapping("/quiz/{quizId}/take/{attemptId}/submit")
    public String submitQuiz(@PathVariable Long quizId,
                            @PathVariable Long attemptId,
                            @RequestParam Map<String, String> answers,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }

        Long userId = (Long) session.getAttribute("userId");
        
        Optional<QuizAttempt> attemptOpt = quizAttemptService.findById(attemptId);
        if (attemptOpt.isEmpty()) {
            return "redirect:/dashboard";
        }

        QuizAttempt attempt = attemptOpt.get();
        
        // Verify that this attempt belongs to the current user
        if (!attempt.getUserId().equals(userId) || !attempt.getQuizId().equals(quizId)) {
            return "redirect:/dashboard";
        }

        // Check if already completed
        if (attempt.isCompleted()) {
            return "redirect:/student/quiz/" + quizId + "/result/" + attemptId;
        }

        try {
            // Calculate score
            List<Question> questions = quizService.getQuestionsByQuizId(quizId);
            int correctAnswers = 0;
            int totalQuestions = questions.size();

            Map<Long, String> userAnswers = new HashMap<>();
            for (Question question : questions) {
                String userAnswer = answers.get("question_" + question.getId());
                if (userAnswer != null) {
                    userAnswers.put(question.getId(), userAnswer);
                    
                    // Check if answer is correct
                    if (userAnswer.equals(question.getCorrectAnswer())) {
                        correctAnswers++;
                    }
                }
            }

            // Calculate percentage
            double percentage = totalQuestions > 0 ? (double) correctAnswers / totalQuestions * 100 : 0;

            // Update attempt
            attempt.setEndTime(LocalDateTime.now());
            attempt.setCompleted(true);
            attempt.setScore(correctAnswers);
            attempt.setTotalQuestions(totalQuestions);
            attempt.setPercentage(percentage);
            attempt.setCorrectAnswers(correctAnswers);
            attempt.setWrongAnswers(totalQuestions - correctAnswers);
            // Note: answers are stored separately as Answer entities, not as a string

            quizAttemptService.updateQuizAttempt(attempt);

            redirectAttributes.addFlashAttribute("successMessage", "Quiz submitted successfully!");
            return "redirect:/student/quiz/" + quizId + "/result/" + attemptId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to submit quiz: " + e.getMessage());
            return "redirect:/student/quiz/" + quizId + "/take/" + attemptId;
        }
    }

    @GetMapping("/quiz/{quizId}/result/{attemptId}")
    public String viewQuizResult(@PathVariable Long quizId,
                                @PathVariable Long attemptId,
                                Model model,
                                HttpSession session) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }

        Long userId = (Long) session.getAttribute("userId");
        
        Optional<QuizAttempt> attemptOpt = quizAttemptService.findById(attemptId);
        if (attemptOpt.isEmpty()) {
            return "redirect:/dashboard";
        }

        QuizAttempt attempt = attemptOpt.get();
        
        // Verify that this attempt belongs to the current user
        if (!attempt.getUserId().equals(userId) || !attempt.getQuizId().equals(quizId)) {
            return "redirect:/dashboard";
        }

        Optional<Quiz> quizOpt = quizService.findById(quizId);
        if (quizOpt.isEmpty()) {
            return "redirect:/dashboard";
        }

        Quiz quiz = quizOpt.get();
        List<Question> questions = quizService.getQuestionsByQuizId(quizId);

        model.addAttribute("quiz", quiz);
        model.addAttribute("attempt", attempt);
        model.addAttribute("questions", questions);
        model.addAttribute("pageTitle", "Result: " + quiz.getTitle());

        return "student/quiz-result";
    }

    @GetMapping("/progress")
    public String viewProgress(Model model, HttpSession session) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }

        Long userId = (Long) session.getAttribute("userId");
        
        List<QuizAttempt> attempts = quizAttemptService.getAttemptsByUser(userId);
        
        // Calculate statistics
        long completedQuizzes = attempts.stream().filter(QuizAttempt::isCompleted).count();
        double averageScore = attempts.stream()
                .filter(QuizAttempt::isCompleted)
                .mapToDouble(QuizAttempt::getPercentage)
                .average()
                .orElse(0.0);

        model.addAttribute("attempts", attempts);
        model.addAttribute("completedQuizzes", completedQuizzes);
        model.addAttribute("averageScore", Math.round(averageScore * 100.0) / 100.0);
        model.addAttribute("pageTitle", "My Progress - QWIZZ");

        return "student/progress";
    }

    @GetMapping("/leaderboard")
    public String viewLeaderboard(Model model, HttpSession session) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }

        // For now, we'll show a simple leaderboard based on average scores
        List<User> students = userService.getUsersByRole(Role.STUDENT);
        
        model.addAttribute("students", students);
        model.addAttribute("pageTitle", "Leaderboard - QWIZZ");

        return "student/leaderboard";
    }
}
