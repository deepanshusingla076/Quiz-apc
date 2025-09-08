package com.qwizz.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leaderboards")
public class Leaderboard {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaderboardType type;
    
    @Column(name = "score_value", nullable = false)
    private Double scoreValue;
    
    @Column(name = "period_start")
    private LocalDateTime periodStart;
    
    @Column(name = "period_end")
    private LocalDateTime periodEnd;
    
    @Column(name = "rank_position")
    private Integer rankPosition;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum LeaderboardType {
        GLOBAL_POINTS,
        QUIZ_HIGH_SCORE,
        CATEGORY_POINTS,
        WEEKLY_POINTS,
        MONTHLY_POINTS,
        QUIZ_STREAK,
        AVERAGE_SCORE
    }
    
    // Constructors
    public Leaderboard() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public Leaderboard(User user, LeaderboardType type, Double scoreValue) {
        this();
        this.user = user;
        this.type = type;
        this.scoreValue = scoreValue;
    }
    
    public Leaderboard(User user, Quiz quiz, LeaderboardType type, Double scoreValue) {
        this(user, type, scoreValue);
        this.quiz = quiz;
    }
    
    public Leaderboard(User user, Category category, LeaderboardType type, Double scoreValue) {
        this(user, type, scoreValue);
        this.category = category;
    }
    
    // Lifecycle callbacks
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
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
    
    public Quiz getQuiz() {
        return quiz;
    }
    
    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public LeaderboardType getType() {
        return type;
    }
    
    public void setType(LeaderboardType type) {
        this.type = type;
    }
    
    public Double getScoreValue() {
        return scoreValue;
    }
    
    public void setScoreValue(Double scoreValue) {
        this.scoreValue = scoreValue;
    }
    
    public LocalDateTime getPeriodStart() {
        return periodStart;
    }
    
    public void setPeriodStart(LocalDateTime periodStart) {
        this.periodStart = periodStart;
    }
    
    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }
    
    public void setPeriodEnd(LocalDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }
    
    public Integer getRankPosition() {
        return rankPosition;
    }
    
    public void setRankPosition(Integer rankPosition) {
        this.rankPosition = rankPosition;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "Leaderboard{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : null) +
                ", quiz=" + (quiz != null ? quiz.getTitle() : null) +
                ", category=" + (category != null ? category.getName() : null) +
                ", type=" + type +
                ", scoreValue=" + scoreValue +
                ", rankPosition=" + rankPosition +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
