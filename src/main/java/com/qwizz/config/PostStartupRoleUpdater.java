package com.qwizz.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostStartupRoleUpdater {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void updateUserRolesAfterStartup() {
        // Wait a bit to ensure all initialization is complete
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            // Check if role column exists and users exist
            Integer adminCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = 'admin'", Integer.class);
            Integer teacherCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = 'john_doe'", Integer.class);
            Integer studentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = 'jane_smith'", Integer.class);

            if (adminCount != null && adminCount > 0) {
                jdbcTemplate.update("UPDATE users SET role = ? WHERE username = ?", "ADMIN", "admin");
                System.out.println("Updated admin user role to ADMIN");
            }
            if (teacherCount != null && teacherCount > 0) {
                jdbcTemplate.update("UPDATE users SET role = ? WHERE username = ?", "TEACHER", "john_doe");
                System.out.println("Updated john_doe user role to TEACHER");
            }
            if (studentCount != null && studentCount > 0) {
                jdbcTemplate.update("UPDATE users SET role = ? WHERE username = ?", "STUDENT", "jane_smith");
                System.out.println("Updated jane_smith user role to STUDENT");
            }

            System.out.println("User roles updated successfully after application startup!");
        } catch (Exception e) {
            System.err.println("Error updating user roles after startup: " + e.getMessage());
        }
    }
}
