package com.qwizz.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "answers")
public class Answer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_attempt_id", nullable = false)
    private QuizAttempt quizAttempt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    @Column(name = "selected_option", columnDefinition = "TEXT")
    private String selectedOption;
    
    @Column(name = "text_answer", columnDefinition = "TEXT")
    private String textAnswer;
    
    @Column(name = "is_correct", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isCorrect = false;
    
    @Column(name = "points_earned", columnDefinition = "INT DEFAULT 0")
    private Integer pointsEarned = 0;
    
    @Column(name = "time_taken")
    private Integer timeTaken; // seconds
    
    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;
    
    // Constructors
    public Answer() {
        this.answeredAt = LocalDateTime.now();
    }
    
    public Answer(QuizAttempt quizAttempt, Question question) {
        this();
        this.quizAttempt = quizAttempt;
        this.question = question;
    }
    
    public Answer(QuizAttempt quizAttempt, Question question, String selectedOption, Boolean isCorrect) {
        this(quizAttempt, question);
        this.selectedOption = selectedOption;
        this.isCorrect = isCorrect;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public QuizAttempt getQuizAttempt() {
        return quizAttempt;
    }
    
    public void setQuizAttempt(QuizAttempt quizAttempt) {
        this.quizAttempt = quizAttempt;
    }
    
    public Question getQuestion() {
        return question;
    }
    
    public void setQuestion(Question question) {
        this.question = question;
    }
    
    public String getSelectedOption() {
        return selectedOption;
    }
    
    public void setSelectedOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }
    
    public String getTextAnswer() {
        return textAnswer;
    }
    
    public void setTextAnswer(String textAnswer) {
        this.textAnswer = textAnswer;
    }
    
    public Boolean getIsCorrect() {
        return isCorrect;
    }
    
    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
    
    public Integer getPointsEarned() {
        return pointsEarned;
    }
    
    public void setPointsEarned(Integer pointsEarned) {
        this.pointsEarned = pointsEarned;
    }
    
    public Integer getTimeTaken() {
        return timeTaken;
    }
    
    public void setTimeTaken(Integer timeTaken) {
        this.timeTaken = timeTaken;
    }
    
    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }
    
    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }
    
    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", quizAttempt=" + (quizAttempt != null ? quizAttempt.getId() : null) +
                ", question=" + (question != null ? question.getId() : null) +
                ", selectedOption='" + selectedOption + '\'' +
                ", isCorrect=" + isCorrect +
                ", pointsEarned=" + pointsEarned +
                ", answeredAt=" + answeredAt +
                '}';
    }
}
