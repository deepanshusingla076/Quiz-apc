package com.qwizz.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "creator_id", nullable = false)
    @NotNull(message = "Creator is required")
    private Long creatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", insertable = false, updatable = false)
    private User creator;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty = Difficulty.MEDIUM;

    @Column(name = "time_limit")
    @Positive(message = "Time limit must be positive")
    private int timeLimit = 30; // in minutes

    @Column(name = "is_public")
    private boolean isPublic = true;

    @Column(name = "ai_generated")
    private boolean aiGenerated = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "active")
    private boolean active = true;
    
    // Additional fields from schema
    @Column(name = "total_attempts", columnDefinition = "INT DEFAULT 0")
    private Integer totalAttempts = 0;
    
    @Column(name = "average_score", columnDefinition = "DECIMAL(5,2) DEFAULT 0.00")
    private Double averageScore = 0.0;
    
    @Column(name = "max_attempts")
    private Integer maxAttempts;
    
    @Column(name = "passing_score")
    private Integer passingScore;
    
    @Column(name = "show_correct_answers", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean showCorrectAnswers = true;
    
    @Column(name = "randomize_questions", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean randomizeQuestions = false;
    
    @Column(name = "instant_feedback", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean instantFeedback = true;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuizAttempt> attempts = new ArrayList<>();

    // Constructors
    public Quiz() {
    }

    public Quiz(String title, String description, Long creatorId, Difficulty difficulty, int timeLimit, boolean isPublic) {
        this.title = title;
        this.description = description;
        this.creatorId = creatorId;
        this.difficulty = difficulty != null ? difficulty : Difficulty.MEDIUM;
        this.timeLimit = timeLimit;
        this.isPublic = isPublic;
        this.aiGenerated = false;
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

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean isAiGenerated() {
        return aiGenerated;
    }

    public void setAiGenerated(boolean aiGenerated) {
        this.aiGenerated = aiGenerated;
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

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public List<QuizAttempt> getAttempts() {
        return attempts;
    }

    public void setAttempts(List<QuizAttempt> attempts) {
        this.attempts = attempts;
    }
    
    // Additional getters and setters
    public Integer getTotalAttempts() {
        return totalAttempts;
    }
    
    public void setTotalAttempts(Integer totalAttempts) {
        this.totalAttempts = totalAttempts;
    }
    
    public Double getAverageScore() {
        return averageScore;
    }
    
    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }
    
    public Integer getMaxAttempts() {
        return maxAttempts;
    }
    
    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
    
    public Integer getPassingScore() {
        return passingScore;
    }
    
    public void setPassingScore(Integer passingScore) {
        this.passingScore = passingScore;
    }
    
    public Boolean getShowCorrectAnswers() {
        return showCorrectAnswers;
    }
    
    public void setShowCorrectAnswers(Boolean showCorrectAnswers) {
        this.showCorrectAnswers = showCorrectAnswers;
    }
    
    public Boolean getRandomizeQuestions() {
        return randomizeQuestions;
    }
    
    public void setRandomizeQuestions(Boolean randomizeQuestions) {
        this.randomizeQuestions = randomizeQuestions;
    }
    
    public Boolean getInstantFeedback() {
        return instantFeedback;
    }
    
    public void setInstantFeedback(Boolean instantFeedback) {
        this.instantFeedback = instantFeedback;
    }
    
    // Utility methods
    public void incrementTotalAttempts() {
        this.totalAttempts = (this.totalAttempts == null ? 0 : this.totalAttempts) + 1;
    }
    
    public void updateAverageScore(double newScore) {
        if (this.totalAttempts == null || this.totalAttempts == 0) {
            this.averageScore = newScore;
        } else {
            // Ensure averageScore is not null before calculation
            double currentAverage = this.averageScore != null ? this.averageScore : 0.0;
            double totalScore = currentAverage * (this.totalAttempts - 1);
            this.averageScore = (totalScore + newScore) / this.totalAttempts;
        }
    }

    public int getQuestionCount() {
        return questions != null ? questions.size() : 0;
    }

    public int getTotalPoints() {
        return questions != null ? questions.stream().mapToInt(Question::getPoints).sum() : 0;
    }

    @Override
    public String toString() {
        return "Quiz{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", creatorId=" + creatorId +
                ", difficulty=" + difficulty +
                ", timeLimit=" + timeLimit +
                ", isPublic=" + isPublic +
                ", aiGenerated=" + aiGenerated +
                ", active=" + active +
                '}';
    }
}
