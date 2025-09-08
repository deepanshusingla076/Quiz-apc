package com.qwizz.controller;

import com.qwizz.model.User;
import com.qwizz.model.Quiz;
import com.qwizz.model.QuizAttempt;
import com.qwizz.service.UserService;
import com.qwizz.service.QuizService;
import com.qwizz.service.QuizAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuizAttemptService quizAttemptService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (session.getAttribute("isLoggedIn") == null) {
            return "redirect:/auth/login";
        }

        Long userId = (Long) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        String firstName = (String) session.getAttribute("firstName");

        // Get user's quizzes
        List<Quiz> userQuizzes = quizService.getQuizzesByCreator(userId);

        // Get user's quiz attempts
        List<QuizAttempt> userAttempts = quizAttemptService.getUserAttempts(userId);

        // Get statistics
        long totalQuizzes = quizService.getQuizCount(userId);
        long totalAttempts = quizAttemptService.getTotalAttempts(userId);
        long completedAttempts = quizAttemptService.getCompletedAttempts(userId);
        double averageScore = quizAttemptService.getAverageScore(userId);

        model.addAttribute("pageTitle", "Dashboard - QWIZZ");
        model.addAttribute("username", username);
        model.addAttribute("firstName", firstName);
        model.addAttribute("userQuizzes", userQuizzes);
        model.addAttribute("userAttempts", userAttempts);
        model.addAttribute("totalQuizzes", totalQuizzes);
        model.addAttribute("totalAttempts", totalAttempts);
        model.addAttribute("completedAttempts", completedAttempts);
        model.addAttribute("averageScore", Math.round(averageScore * 100.0) / 100.0);

        return "user/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        if (session.getAttribute("isLoggedIn") == null) {
            return "redirect:/auth/login";
        }

        Long userId = (Long) session.getAttribute("userId");
        Optional<User> userOpt = userService.findById(userId);

        if (userOpt.isEmpty()) {
            return "redirect:/auth/logout";
        }

        User user = userOpt.get();
        model.addAttribute("pageTitle", "Profile - QWIZZ");
        model.addAttribute("user", user);

        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (session.getAttribute("isLoggedIn") == null) {
            return "redirect:/auth/login";
        }

        try {
            Long userId = (Long) session.getAttribute("userId");
            User updatedUser = userService.updateUserProfile(userId, firstName, lastName, email);

            // Update session attributes
            session.setAttribute("firstName", updatedUser.getFirstName());

            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile: " + e.getMessage());
        }

        return "redirect:/user/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (session.getAttribute("isLoggedIn") == null) {
            return "redirect:/auth/login";
        }

        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match");
                return "redirect:/user/profile";
            }

            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("errorMessage", "New password must be at least 6 characters long");
                return "redirect:/user/profile";
            }

            Long userId = (Long) session.getAttribute("userId");
            userService.changePassword(userId, oldPassword, newPassword);

            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to change password: " + e.getMessage());
        }

        return "redirect:/user/profile";
    }

    @GetMapping("/attempts")
    public String userAttempts(Model model, HttpSession session) {
        if (session.getAttribute("isLoggedIn") == null) {
            return "redirect:/auth/login";
        }

        Long userId = (Long) session.getAttribute("userId");
        List<QuizAttempt> attempts = quizAttemptService.getUserAttempts(userId);

        model.addAttribute("pageTitle", "My Quiz Attempts - QWIZZ");
        model.addAttribute("attempts", attempts);

        return "user/attempts";
    }

    @GetMapping("/quizzes")
    public String userQuizzes(Model model, HttpSession session) {
        if (session.getAttribute("isLoggedIn") == null) {
            return "redirect:/auth/login";
        }

        Long userId = (Long) session.getAttribute("userId");
        List<Quiz> quizzes = quizService.getQuizzesByCreator(userId);

        model.addAttribute("pageTitle", "My Quizzes - QWIZZ");
        model.addAttribute("quizzes", quizzes);

        return "user/quizzes";
    }
}
