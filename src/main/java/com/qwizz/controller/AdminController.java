package com.qwizz.controller;

import com.qwizz.model.User;
import com.qwizz.service.QuizService;
import com.qwizz.service.ResultService;
import com.qwizz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Admin-specific controller for system administration and analytics
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private ResultService resultService;

    @Autowired
    private UserService userService;

    // Admin Dashboard
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal User admin, Model model) {
        // Get system-wide analytics
        List<User> allUsers = userService.getAllUsers();
        long totalUsers = allUsers.size();
        long totalStudents = allUsers.stream().filter(u -> u.getRole().name().equals("STUDENT")).count();
        long totalTeachers = allUsers.stream().filter(u -> u.getRole().name().equals("TEACHER")).count();
        
        // Get quiz statistics
        List<com.qwizz.model.Quiz> allQuizzes = quizService.getAllQuizzes();
        long totalQuizzes = allQuizzes.size();
        long publicQuizzes = allQuizzes.stream().filter(com.qwizz.model.Quiz::isPublic).count();
        
        model.addAttribute("admin", admin);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalTeachers", totalTeachers);
        model.addAttribute("totalQuizzes", totalQuizzes);
        model.addAttribute("publicQuizzes", publicQuizzes);
        model.addAttribute("recentUsers", allUsers.stream().limit(10).toList());
        model.addAttribute("recentQuizzes", allQuizzes.stream().limit(10).toList());
        
        return "admin/dashboard";
    }

    // User Management
    @GetMapping("/users")
    public String manageUsers(@AuthenticationPrincipal User admin, Model model) {
        List<User> allUsers = userService.getAllUsers();
        model.addAttribute("admin", admin);
        model.addAttribute("users", allUsers);
        return "admin/users";
    }

    // Quiz Management
    @GetMapping("/quizzes")
    public String manageQuizzes(@AuthenticationPrincipal User admin, Model model) {
        List<com.qwizz.model.Quiz> allQuizzes = quizService.getAllQuizzes();
        model.addAttribute("admin", admin);
        model.addAttribute("quizzes", allQuizzes);
        return "admin/quizzes";
    }

    // System Analytics
    @GetMapping("/analytics")
    public String systemAnalytics(@AuthenticationPrincipal User admin, Model model) {
        // System-wide analytics would go here
        model.addAttribute("admin", admin);
        return "admin/analytics";
    }
}
