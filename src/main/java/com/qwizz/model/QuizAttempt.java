package com.qwizz.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;
import java.time.Duration;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "User ID is required")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "quiz_id", nullable = false)
    @NotNull(message = "Quiz ID is required")
    private Long quizId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", insertable = false, updatable = false)
    private Quiz quiz;

    @Column(nullable = false)
    @PositiveOrZero(message = "Score must be zero or positive")
    private int score = 0;

    @Column(name = "total_questions", nullable = false)
    @PositiveOrZero(message = "Total questions must be zero or positive")
    private int totalQuestions;

    @Column(name = "correct_answers")
    @PositiveOrZero(message = "Correct answers must be zero or positive")
    private int correctAnswers = 0;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "completed")
    private boolean completed = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status = AttemptStatus.STARTED;

    // Constructors
    public QuizAttempt() {
    }

    public QuizAttempt(Long userId, Long quizId, int totalQuestions) {
        this.userId = userId;
        this.quizId = quizId;
        this.totalQuestions = totalQuestions;
        this.score = 0;
        this.correctAnswers = 0;
        this.startTime = LocalDateTime.now();
        this.completed = false;
        this.status = AttemptStatus.STARTED;
    }

    @PrePersist
    protected void onCreate() {
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && endTime == null) {
            this.endTime = LocalDateTime.now();
            this.status = AttemptStatus.COMPLETED;
        }
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    public double getPercentage() {
        if (totalQuestions == 0)
            return 0.0;
        return (double) correctAnswers / totalQuestions * 100;
    }

    public String getFormattedPercentage() {
        return String.format("%.1f%%", getPercentage());
    }

    public Duration getDuration() {
        if (startTime == null) return Duration.ZERO;
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return Duration.between(startTime, end);
    }

    public String getFormattedDuration() {
        Duration duration = getDuration();
        long minutes = duration.toMinutes();
        long seconds = duration.getSeconds() % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public boolean isInProgress() {
        return status == AttemptStatus.STARTED && !completed;
    }

    public boolean isPassed() {
        return getPercentage() >= 50.0; // 50% passing grade
    }

    @Override
    public String toString() {
        return "QuizAttempt{" +
                "id=" + id +
                ", userId=" + userId +
                ", quizId=" + quizId +
                ", score=" + score +
                ", correctAnswers=" + correctAnswers +
                ", totalQuestions=" + totalQuestions +
                ", completed=" + completed +
                ", status=" + status +
                '}';
    }
}
