package com.qwizz.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Column(name = "first_name", nullable = false, length = 50)
    @NotBlank(message = "First name is required")
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.STUDENT;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "active")
    private boolean active = true;

    // Extended profile fields
    @Column(name = "profile_picture", length = 255)
    private String profilePicture;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    @Column(name = "date_of_birth")
    private java.sql.Date dateOfBirth;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 100)
    private String location;
    
    // Game statistics
    @Column(name = "total_points", columnDefinition = "INT DEFAULT 0")
    private Integer totalPoints = 0;
    
    @Column(name = "quiz_streak", columnDefinition = "INT DEFAULT 0")
    private Integer quizStreak = 0;
    
    @Column(name = "total_quizzes_taken", columnDefinition = "INT DEFAULT 0")
    private Integer totalQuizzesTaken = 0;
    
    @Column(name = "total_quizzes_created", columnDefinition = "INT DEFAULT 0")
    private Integer totalQuizzesCreated = 0;
    
    @Column(name = "average_score", columnDefinition = "DECIMAL(5,2) DEFAULT 0.00")
    private Double averageScore = 0.0;
    
    @Column(name = "email_verified", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean emailVerified = false;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    // Relationships
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Quiz> createdQuizzes;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuizAttempt> quizAttempts;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserAchievement> userAchievements;

    // Constructors
    public User() {
    }

    public User(String username, String email, String password, String firstName, String lastName, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role != null ? role : Role.STUDENT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.active = true;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // UserDetails implementation for Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.getValue()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isTeacher() {
        return role == Role.TEACHER;
    }

    public boolean isStudent() {
        return role == Role.STUDENT;
    }
    
    // Extended profile getters and setters
    public String getProfilePicture() {
        return profilePicture;
    }
    
    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public java.sql.Date getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(java.sql.Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    // Game statistics getters and setters
    public Integer getTotalPoints() {
        return totalPoints;
    }
    
    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }
    
    public Integer getQuizStreak() {
        return quizStreak;
    }
    
    public void setQuizStreak(Integer quizStreak) {
        this.quizStreak = quizStreak;
    }
    
    public Integer getTotalQuizzesTaken() {
        return totalQuizzesTaken;
    }
    
    public void setTotalQuizzesTaken(Integer totalQuizzesTaken) {
        this.totalQuizzesTaken = totalQuizzesTaken;
    }
    
    public Integer getTotalQuizzesCreated() {
        return totalQuizzesCreated;
    }
    
    public void setTotalQuizzesCreated(Integer totalQuizzesCreated) {
        this.totalQuizzesCreated = totalQuizzesCreated;
    }
    
    public Double getAverageScore() {
        return averageScore;
    }
    
    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }
    
    public Boolean getEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    // Relationship getters and setters
    public List<Quiz> getCreatedQuizzes() {
        return createdQuizzes;
    }
    
    public void setCreatedQuizzes(List<Quiz> createdQuizzes) {
        this.createdQuizzes = createdQuizzes;
    }
    
    public List<QuizAttempt> getQuizAttempts() {
        return quizAttempts;
    }
    
    public void setQuizAttempts(List<QuizAttempt> quizAttempts) {
        this.quizAttempts = quizAttempts;
    }
    
    public List<UserAchievement> getUserAchievements() {
        return userAchievements;
    }
    
    public void setUserAchievements(List<UserAchievement> userAchievements) {
        this.userAchievements = userAchievements;
    }
    
    // Utility methods
    public void incrementQuizzesTaken() {
        this.totalQuizzesTaken = (this.totalQuizzesTaken == null ? 0 : this.totalQuizzesTaken) + 1;
    }
    
    public void incrementQuizzesCreated() {
        this.totalQuizzesCreated = (this.totalQuizzesCreated == null ? 0 : this.totalQuizzesCreated) + 1;
    }
    
    public void addPoints(int points) {
        this.totalPoints = (this.totalPoints == null ? 0 : this.totalPoints) + points;
    }
    
    public void updateAverageScore(double newScore) {
        if (this.totalQuizzesTaken == null || this.totalQuizzesTaken == 0) {
            this.averageScore = newScore;
        } else {
            double totalScore = this.averageScore * (this.totalQuizzesTaken - 1);
            this.averageScore = (totalScore + newScore) / this.totalQuizzesTaken;
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", role=" + role +
                ", active=" + active +
                '}';
    }
}
