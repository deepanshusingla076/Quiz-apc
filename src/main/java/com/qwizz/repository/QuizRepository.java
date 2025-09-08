package com.qwizz.repository;

import com.qwizz.model.Difficulty;
import com.qwizz.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByCreatorIdAndActiveTrue(Long creatorId);
    
    List<Quiz> findByIsPublicTrueAndActiveTrue();
    
    List<Quiz> findByDifficultyAndIsPublicTrueAndActiveTrue(Difficulty difficulty);
    
    @Query("SELECT q FROM Quiz q WHERE q.title LIKE %:searchTerm% AND q.isPublic = true AND q.active = true ORDER BY q.createdAt DESC")
    List<Quiz> searchByTitle(@Param("searchTerm") String searchTerm);
    
    long countByCreatorIdAndActiveTrue(Long creatorId);
    
    List<Quiz> findByActiveTrueOrderByCreatedAtDesc();
    
    // Additional methods for enhanced features
    
    // Find quizzes by category
    List<Quiz> findByCategoryIdAndIsPublicTrueAndActiveTrueOrderByCreatedAtDesc(Long categoryId);
    
    // Find quizzes by category and difficulty
    List<Quiz> findByCategoryIdAndDifficultyAndIsPublicTrueAndActiveTrue(Long categoryId, Difficulty difficulty);
    
    // Find popular quizzes (most attempts)
    @Query("SELECT q FROM Quiz q WHERE q.isPublic = true AND q.active = true ORDER BY q.totalAttempts DESC")
    List<Quiz> findPopularQuizzes();
    
    // Find highest rated quizzes
    @Query("SELECT q FROM Quiz q WHERE q.isPublic = true AND q.active = true AND q.averageScore IS NOT NULL ORDER BY q.averageScore DESC")
    List<Quiz> findHighestRatedQuizzes();
    
    // Find AI generated quizzes
    List<Quiz> findByAiGeneratedTrueAndIsPublicTrueAndActiveTrue();
    
    // Advanced search with multiple criteria
    @Query("SELECT q FROM Quiz q WHERE q.isPublic = true AND q.active = true " +
           "AND (:categoryId IS NULL OR q.category.id = :categoryId) " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
           "AND (:searchTerm IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(q.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY q.createdAt DESC")
    List<Quiz> findQuizzesWithFilters(@Param("categoryId") Long categoryId, 
                                     @Param("difficulty") Difficulty difficulty, 
                                     @Param("searchTerm") String searchTerm);
    
    // Find quizzes created by user in category
    List<Quiz> findByCreatorIdAndCategoryIdAndActiveTrue(Long creatorId, Long categoryId);
    
    // Find recent quizzes
    @Query("SELECT q FROM Quiz q WHERE q.isPublic = true AND q.active = true AND q.createdAt >= :since ORDER BY q.createdAt DESC")
    List<Quiz> findRecentQuizzes(@Param("since") java.time.LocalDateTime since);
    
    // Count quizzes by category
    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.category.id = :categoryId AND q.active = true")
    int countByCategoryId(@Param("categoryId") Long categoryId);
    
    // Find quizzes user hasn't attempted
    @Query("SELECT q FROM Quiz q WHERE q.isPublic = true AND q.active = true " +
           "AND q.id NOT IN (SELECT qa.quiz.id FROM QuizAttempt qa WHERE qa.user.id = :userId)")
    List<Quiz> findQuizzesNotAttemptedByUser(@Param("userId") Long userId);
    
    // Find quizzes similar to given quiz (same category, similar difficulty)
    @Query("SELECT q FROM Quiz q WHERE q.isPublic = true AND q.active = true " +
           "AND q.id != :quizId AND q.category.id = :categoryId " +
           "ORDER BY ABS(CAST(q.difficulty AS INTEGER) - :difficultyValue)")
    List<Quiz> findSimilarQuizzes(@Param("quizId") Long quizId, 
                                 @Param("categoryId") Long categoryId, 
                                 @Param("difficultyValue") int difficultyValue);
}
