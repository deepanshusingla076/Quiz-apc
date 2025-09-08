package com.qwizz.controller;

import com.qwizz.config.JwtUtil;
import com.qwizz.model.Role;
import com.qwizz.model.User;
import com.qwizz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("pageTitle", "Login - QWIZZ");
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       @RequestParam(required = false) String role,
                       HttpServletResponse response,
                       RedirectAttributes redirectAttributes) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = (User) userDetails;

            // Validate role if specified
            if (role != null && !role.isEmpty()) {
                if (!user.getRole().toString().equalsIgnoreCase(role)) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Invalid role. Please select the correct role for your account.");
                    return "redirect:/login";
                }
            }

            // Generate JWT token
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole().toString());
            claims.put("userId", user.getId());
            String token = jwtUtil.generateToken(userDetails, claims);

            // Set JWT token as HTTP-only cookie
            Cookie jwtCookie = new Cookie("jwt-token", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60); // 24 hours
            response.addCookie(jwtCookie);

            redirectAttributes.addFlashAttribute("successMessage",
                "Welcome back, " + user.getFirstName() + "!");

            // Redirect based on role
            return switch (user.getRole()) {
                case TEACHER -> "redirect:/teacher/dashboard";
                case STUDENT -> "redirect:/student/dashboard";
                case ADMIN -> "redirect:/admin/dashboard";
            };

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid username or password");
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("pageTitle", "Register - QWIZZ");
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user,
                          @RequestParam String confirmPassword,
                          @RequestParam String role,
                          RedirectAttributes redirectAttributes) {
        try {
            // Validate passwords match
            if (!user.getPassword().equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match");
                return "redirect:/register";
            }

            // Validate required fields
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Username is required");
                return "redirect:/register";
            }

            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email is required");
                return "redirect:/register";
            }

            if (user.getPassword() == null || user.getPassword().length() < 6) {
                redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters long");
                return "redirect:/register";
            }

            // Set role
            try {
                user.setRole(Role.valueOf(role.toUpperCase()));
            } catch (IllegalArgumentException e) {
                user.setRole(Role.STUDENT); // Default to student
            }

            User savedUser = userService.registerUser(user);
            redirectAttributes.addFlashAttribute("successMessage",
                "Registration successful! Welcome to QWIZZ, " + savedUser.getFirstName() + "!");

            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response, RedirectAttributes redirectAttributes) {
        // Clear JWT cookie
        Cookie jwtCookie = new Cookie("jwt-token", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);

        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out successfully");
        return "redirect:/";
    }

    // Dashboard redirect based on role
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            return switch (user.getRole()) {
                case TEACHER -> "redirect:/teacher/dashboard";
                case STUDENT -> "redirect:/student/dashboard";
                case ADMIN -> "redirect:/admin/dashboard";
            };
        }
        return "redirect:/login";
    }

    // REST API endpoints for JWT authentication
    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiLogin(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = (User) userDetails;

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole().toString());
            claims.put("userId", user.getId());
            String token = jwtUtil.generateToken(userDetails, claims);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "role", user.getRole().toString()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid credentials");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiRegister(@RequestBody Map<String, String> registerRequest) {
        try {
            User user = new User();
            user.setUsername(registerRequest.get("username"));
            user.setEmail(registerRequest.get("email"));
            user.setPassword(registerRequest.get("password"));
            user.setFirstName(registerRequest.get("firstName"));
            user.setLastName(registerRequest.get("lastName"));
            
            String roleStr = registerRequest.getOrDefault("role", "STUDENT");
            user.setRole(Role.valueOf(roleStr.toUpperCase()));

            User savedUser = userService.registerUser(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful");
            response.put("user", Map.of(
                "id", savedUser.getId(),
                "username", savedUser.getUsername(),
                "email", savedUser.getEmail(),
                "firstName", savedUser.getFirstName(),
                "lastName", savedUser.getLastName(),
                "role", savedUser.getRole().toString()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        model.addAttribute("pageTitle", "Forgot Password - QWIZZ");
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email,
                                RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                // In a real application, you would send a password reset email
                redirectAttributes.addFlashAttribute("successMessage",
                    "If an account with that email exists, a password reset link has been sent.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                    "If an account with that email exists, a password reset link has been sent.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred. Please try again.");
        }

        return "redirect:/forgot-password";
    }
}
