package com.qwizz.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievements")
public class UserAchievement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;
    
    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;
    
    @Column(name = "progress_value")
    private Integer progressValue;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    // Constructors
    public UserAchievement() {
        this.earnedAt = LocalDateTime.now();
    }
    
    public UserAchievement(User user, Achievement achievement) {
        this();
        this.user = user;
        this.achievement = achievement;
    }
    
    public UserAchievement(User user, Achievement achievement, Integer progressValue) {
        this(user, achievement);
        this.progressValue = progressValue;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Achievement getAchievement() {
        return achievement;
    }
    
    public void setAchievement(Achievement achievement) {
        this.achievement = achievement;
    }
    
    public LocalDateTime getEarnedAt() {
        return earnedAt;
    }
    
    public void setEarnedAt(LocalDateTime earnedAt) {
        this.earnedAt = earnedAt;
    }
    
    public Integer getProgressValue() {
        return progressValue;
    }
    
    public void setProgressValue(Integer progressValue) {
        this.progressValue = progressValue;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @Override
    public String toString() {
        return "UserAchievement{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : null) +
                ", achievement=" + (achievement != null ? achievement.getName() : null) +
                ", earnedAt=" + earnedAt +
                ", progressValue=" + progressValue +
                '}';
    }
}
