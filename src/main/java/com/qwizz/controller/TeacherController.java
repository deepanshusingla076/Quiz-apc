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
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Teacher-specific controller for quiz creation, question management, and result viewing
 */
@Controller
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuizAttemptService quizAttemptService;

    @Autowired
    private UserService userService;

    // Get current logged-in user from session
    private User getCurrentUser(HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        String userRole = (String) session.getAttribute("userRole");
        Long userId = (Long) session.getAttribute("userId");
        
        if (isLoggedIn != null && isLoggedIn && "TEACHER".equals(userRole) && userId != null) {
            Optional<User> userOpt = userService.findById(userId);
            return userOpt.orElse(null);
        }
        return null;
    }

    @GetMapping("/quiz/create")
    public String createQuizPage(Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("quiz", new Quiz());
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("pageTitle", "Create Quiz - QWIZZ");
        return "teacher/quiz-create";
    }

    @PostMapping("/quiz/create")
    public String createQuiz(@ModelAttribute Quiz quiz,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            quiz.setCreatorId(currentUser.getId());
            quiz.setCreatedAt(LocalDateTime.now());
            quiz.setUpdatedAt(LocalDateTime.now());
            quiz.setActive(true);

            Quiz savedQuiz = quizService.createQuiz(quiz);
            redirectAttributes.addFlashAttribute("successMessage", "Quiz created successfully!");
            return "redirect:/teacher/quiz/" + savedQuiz.getId() + "/edit";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create quiz: " + e.getMessage());
            return "redirect:/teacher/quiz/create";
        }
    }

    @GetMapping("/quiz/{id}/edit")
    public String editQuizPage(@PathVariable Long id, Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        
        Optional<Quiz> quizOpt = quizService.findById(id);
        if (quizOpt.isEmpty()) {
            return "redirect:/teacher/quizzes";
        }

        Quiz quiz = quizOpt.get();
        
        // Check if teacher owns this quiz
        if (!quiz.getCreatorId().equals(currentUser.getId())) {
            return "redirect:/teacher/quizzes";
        }

        List<Question> questions = quizService.getQuestionsByQuizId(id);

        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        model.addAttribute("newQuestion", new Question());
        model.addAttribute("questionTypes", QuestionType.values());
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("pageTitle", "Edit Quiz: " + quiz.getTitle());

        return "teacher/quiz-edit";
    }

    @PostMapping("/quiz/{id}/edit")
    public String updateQuiz(@PathVariable Long id,
                            @ModelAttribute Quiz quiz,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        
        Optional<Quiz> existingQuizOpt = quizService.findById(id);
        if (existingQuizOpt.isEmpty()) {
            return "redirect:/teacher/quizzes";
        }

        Quiz existingQuiz = existingQuizOpt.get();
        
        // Check if teacher owns this quiz
        if (!existingQuiz.getCreatorId().equals(currentUser.getId())) {
            return "redirect:/teacher/quizzes";
        }

        try {
            quiz.setId(id);
            quiz.setCreatorId(currentUser.getId());
            quiz.setCreatedAt(existingQuiz.getCreatedAt());
            quiz.setUpdatedAt(LocalDateTime.now());
            
            quizService.updateQuiz(quiz);
            redirectAttributes.addFlashAttribute("successMessage", "Quiz updated successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update quiz: " + e.getMessage());
        }

        return "redirect:/teacher/quiz/" + id + "/edit";
    }

    @PostMapping("/quiz/{id}/add-question")
    public String addQuestion(@PathVariable Long id,
                             @ModelAttribute Question question,
                             @RequestParam(required = false) String option1,
                             @RequestParam(required = false) String option2,
                             @RequestParam(required = false) String option3,
                             @RequestParam(required = false) String option4,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        
        Optional<Quiz> quizOpt = quizService.findById(id);
        if (quizOpt.isEmpty()) {
            return "redirect:/teacher/quizzes";
        }

        Quiz quiz = quizOpt.get();
        
        // Check if teacher owns this quiz
        if (!quiz.getCreatorId().equals(currentUser.getId())) {
            return "redirect:/teacher/quizzes";
        }

        try {
            // Set up options for multiple choice questions
            if (QuestionType.MULTIPLE_CHOICE.equals(question.getQuestionType())) {
                List<String> options = new ArrayList<>();
                if (option1 != null && !option1.trim().isEmpty()) options.add(option1.trim());
                if (option2 != null && !option2.trim().isEmpty()) options.add(option2.trim());
                if (option3 != null && !option3.trim().isEmpty()) options.add(option3.trim());
                if (option4 != null && !option4.trim().isEmpty()) options.add(option4.trim());
                question.setOptionsList(options);
            }

            question.setQuizId(id);
            quizService.addQuestionToQuiz(id, question);
            redirectAttributes.addFlashAttribute("successMessage", "Question added successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add question: " + e.getMessage());
        }

        return "redirect:/teacher/quiz/" + id + "/edit";
    }

    @PostMapping("/quiz/{quizId}/question/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long quizId,
                                @PathVariable Long questionId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        
        Optional<Quiz> quizOpt = quizService.findById(quizId);
        if (quizOpt.isEmpty()) {
            return "redirect:/teacher/quizzes";
        }

        Quiz quiz = quizOpt.get();
        
        // Check if teacher owns this quiz
        if (!quiz.getCreatorId().equals(currentUser.getId())) {
            return "redirect:/teacher/quizzes";
        }

        try {
            quizService.deleteQuestion(questionId);
            redirectAttributes.addFlashAttribute("successMessage", "Question deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete question: " + e.getMessage());
        }

        return "redirect:/teacher/quiz/" + quizId + "/edit";
    }

    @GetMapping("/quiz/{id}/preview")
    public String previewQuiz(@PathVariable Long id, Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        
        Optional<Quiz> quizOpt = quizService.findById(id);
        if (quizOpt.isEmpty()) {
            return "redirect:/teacher/quizzes";
        }

        Quiz quiz = quizOpt.get();
        
        // Check if teacher owns this quiz
        if (!quiz.getCreatorId().equals(currentUser.getId())) {
            return "redirect:/teacher/quizzes";
        }

        List<Question> questions = quizService.getQuestionsByQuizId(id);

        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        model.addAttribute("pageTitle", "Preview: " + quiz.getTitle());

        return "teacher/quiz-preview";
    }

    @GetMapping("/quiz/{id}/results")
    public String quizResults(@PathVariable Long id, Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        
        Optional<Quiz> quizOpt = quizService.findById(id);
        if (quizOpt.isEmpty()) {
            return "redirect:/teacher/quizzes";
        }

        Quiz quiz = quizOpt.get();
        
        // Check if teacher owns this quiz
        if (!quiz.getCreatorId().equals(currentUser.getId())) {
            return "redirect:/teacher/quizzes";
        }

        List<QuizAttempt> attempts = quizAttemptService.getAttemptsByQuizId(id);

        model.addAttribute("quiz", quiz);
        model.addAttribute("attempts", attempts);
        model.addAttribute("pageTitle", "Results: " + quiz.getTitle());

        return "teacher/quiz-results";
    }

    @GetMapping("/quizzes")
    public String manageQuizzes(Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Quiz> quizzes = quizService.getQuizzesByCreator(currentUser.getId());

        model.addAttribute("quizzes", quizzes);
        model.addAttribute("pageTitle", "Manage Quizzes - QWIZZ");

        return "teacher/quizzes";
    }

    @PostMapping("/quiz/{id}/delete")
    public String deleteQuiz(@PathVariable Long id,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        
        Optional<Quiz> quizOpt = quizService.findById(id);
        if (quizOpt.isEmpty()) {
            return "redirect:/teacher/quizzes";
        }

        Quiz quiz = quizOpt.get();
        
        // Check if teacher owns this quiz
        if (!quiz.getCreatorId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to delete this quiz");
            return "redirect:/teacher/quizzes";
        }

        try {
            quizService.deleteQuiz(id);
            redirectAttributes.addFlashAttribute("successMessage", "Quiz deleted successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete quiz: " + e.getMessage());
        }

        return "redirect:/teacher/quizzes";
    }

    @GetMapping("/students")
    public String viewStudents(Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<User> students = userService.getUsersByRole(Role.STUDENT);
        model.addAttribute("students", students);
        model.addAttribute("pageTitle", "Students - QWIZZ");

        return "teacher/students";
    }

    @GetMapping("/analytics")
    public String analytics(Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        
        // Get analytics data
        List<Quiz> teacherQuizzes = quizService.getQuizzesByCreator(currentUser.getId());
        List<QuizAttempt> allAttempts = new ArrayList<>();
        
        for (Quiz quiz : teacherQuizzes) {
            allAttempts.addAll(quizAttemptService.getAttemptsByQuizId(quiz.getId()));
        }

        // Calculate analytics
        int totalQuizzes = teacherQuizzes.size();
        int totalAttempts = allAttempts.size();
        double averageScore = allAttempts.stream()
                .filter(QuizAttempt::isCompleted)
                .mapToDouble(attempt -> attempt.getPercentage() != null ? attempt.getPercentage() : 0.0)
                .average()
                .orElse(0.0);

        model.addAttribute("totalQuizzes", totalQuizzes);
        model.addAttribute("totalAttempts", totalAttempts);
        model.addAttribute("averageScore", Math.round(averageScore * 100.0) / 100.0);
        model.addAttribute("teacherQuizzes", teacherQuizzes);
        model.addAttribute("recentAttempts", allAttempts.stream().limit(10).toList());
        model.addAttribute("pageTitle", "Analytics - QWIZZ");

        return "teacher/analytics";
    }

    @GetMapping("/quiz/import")
    public String importQuiz(Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("pageTitle", "Import Quiz - QWIZZ");
        return "teacher/quiz-import";
    }

    @GetMapping("/reports/generate")
    public String generateReports(Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("pageTitle", "Generate Reports - QWIZZ");
        return "teacher/reports";
    }

    @GetMapping("/settings")
    public String settings(Model model, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("pageTitle", "Settings - QWIZZ");
        return "teacher/settings";
    }
}
