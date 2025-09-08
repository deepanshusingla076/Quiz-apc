package com.qwizz.repository;

import com.qwizz.model.AttemptStatus;
import com.qwizz.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByUser_IdOrderByStartTimeDesc(Long userId);
    
    List<QuizAttempt> findByQuiz_IdOrderByStartTimeDesc(Long quizId);
    
    List<QuizAttempt> findByUser_IdAndQuiz_IdOrderByStartTimeDesc(Long userId, Long quizId);
    
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.quiz.id = :quizId AND qa.completed = false ORDER BY qa.startTime DESC")
    Optional<QuizAttempt> findActiveAttemptByUserIdAndQuizId(@Param("userId") Long userId, @Param("quizId") Long quizId);
    
    long countByUser_Id(Long userId);
    
    long countByUser_IdAndCompletedTrue(Long userId);
    
    List<QuizAttempt> findByStatusOrderByStartTimeDesc(AttemptStatus status);
    
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.quiz.creator.id = :creatorId ORDER BY qa.startTime DESC")
    List<QuizAttempt> findByQuizCreatorIdOrderByStartTimeDesc(@Param("creatorId") Long creatorId);
    
    // Additional methods for enhanced features
    
    // Find recent attempts
    List<QuizAttempt> findByUser_IdAndStartTimeAfterOrderByStartTimeDesc(Long userId, LocalDateTime since);
    
    // Find best attempt for user and quiz
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.quiz.id = :quizId " +
           "AND qa.completed = true ORDER BY qa.score DESC, qa.percentageScore DESC")
    Optional<QuizAttempt> findBestAttemptByUserAndQuiz(@Param("userId") Long userId, @Param("quizId") Long quizId);
    
    // Find user's average score
    @Query("SELECT AVG(qa.score) FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.completed = true")
    Double getAverageScoreByUser(@Param("userId") Long userId);
    
    // Find user's completion rate
    @Query("SELECT (COUNT(qa) * 100.0 / (SELECT COUNT(qa2) FROM QuizAttempt qa2 WHERE qa2.user.id = :userId)) " +
           "FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.completed = true")
    Double getCompletionRateByUser(@Param("userId") Long userId);
    
    // Find attempts in time range
    List<QuizAttempt> findByStartTimeBetweenOrderByStartTimeDesc(LocalDateTime start, LocalDateTime end);
    
    // Find high score attempts
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.percentageScore >= :minPercentage AND qa.completed = true " +
           "ORDER BY qa.percentageScore DESC, qa.score DESC")
    List<QuizAttempt> findHighScoreAttempts(@Param("minPercentage") Double minPercentage);
    
    // Find quiz performance statistics
    @Query("SELECT AVG(qa.score), AVG(qa.percentageScore), COUNT(qa) FROM QuizAttempt qa " +
           "WHERE qa.quiz.id = :quizId AND qa.completed = true")
    Object[] getQuizStatistics(@Param("quizId") Long quizId);
    
    // Find user's streak data
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.completed = true " +
           "ORDER BY qa.endTime DESC")
    List<QuizAttempt> findCompletedAttemptsByUserOrderByEndTime(@Param("userId") Long userId);
    
    // Count attempts by category
    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.quiz.category.id = :categoryId AND qa.completed = true")
    int countCompletedAttemptsByCategory(@Param("categoryId") Long categoryId);
    
    // Find attempts by quiz and time range for analytics
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.startTime >= :since " +
           "AND qa.completed = true ORDER BY qa.startTime DESC")
    List<QuizAttempt> findQuizAttemptsForAnalytics(@Param("quizId") Long quizId, @Param("since") LocalDateTime since);
    
    // Find fastest completions
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.timeTaken IS NOT NULL AND qa.completed = true " +
           "ORDER BY qa.timeTaken ASC")
    List<QuizAttempt> findFastestCompletions();
    
    // Find user's rank for specific quiz
    @Query("SELECT COUNT(qa) + 1 FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.completed = true " +
           "AND qa.score > (SELECT qa2.score FROM QuizAttempt qa2 WHERE qa2.user.id = :userId " +
           "AND qa2.quiz.id = :quizId AND qa2.completed = true ORDER BY qa2.score DESC LIMIT 1)")
    Integer findUserRankInQuiz(@Param("userId") Long userId, @Param("quizId") Long quizId);
    
    // Count total attempts on quizzes created by a specific user
    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.quiz.creator.id = :creatorId")
    int countAttemptsByCreatorId(@Param("creatorId") Long creatorId);
}
