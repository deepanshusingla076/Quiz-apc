package com.qwizz.repository;

import com.qwizz.model.Question;
import com.qwizz.model.QuestionType;
import com.qwizz.model.Difficulty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuizIdAndActiveTrue(Long quizId);
    
    List<Question> findByQuestionTypeAndActiveTrue(QuestionType questionType);
    
    @Query("SELECT q FROM Question q JOIN q.quiz qz WHERE qz.difficulty = :difficulty AND q.active = true")
    List<Question> findByQuizDifficultyAndActiveTrue(@Param("difficulty") Difficulty difficulty);
    
    List<Question> findByQuestionTextContainingIgnoreCaseAndActiveTrue(String searchText);
    
    @Query("SELECT q FROM Question q JOIN q.quiz qz WHERE qz.creatorId = :creatorId AND q.active = true")
    List<Question> findByQuizCreatorIdAndActiveTrue(@Param("creatorId") Long creatorId);
    
    // Get random questions using JPQL
    @Query("SELECT q FROM Question q WHERE q.active = true ORDER BY FUNCTION('RAND')")
    List<Question> findRandomQuestions(Pageable pageable);
    
    long countByQuizIdAndActiveTrue(Long quizId);
    
    @Modifying
    @Query("UPDATE Question q SET q.active = false WHERE q.quizId = :quizId")
    void softDeleteByQuizId(@Param("quizId") Long quizId);
    
    // Additional methods for enhanced features
    
    // Find questions ordered by their order in quiz
    List<Question> findByQuizIdAndActiveTrueOrderByQuestionOrder(Long quizId);
    
    // Find questions by difficulty level
    List<Question> findByDifficultyLevelAndActiveTrue(Difficulty difficultyLevel);
    
    // Find questions with explanations
    @Query("SELECT q FROM Question q WHERE q.explanation IS NOT NULL AND q.active = true")
    List<Question> findQuestionsWithExplanations();
    
    // Find questions by type and quiz
    List<Question> findByQuizIdAndQuestionTypeAndActiveTrue(Long quizId, QuestionType questionType);
    
    // Get total points for quiz
    @Query("SELECT SUM(q.points) FROM Question q WHERE q.quizId = :quizId AND q.active = true")
    Integer getTotalPointsByQuizId(@Param("quizId") Long quizId);
    
    // Find hardest questions (by answer statistics)
    @Query("SELECT q FROM Question q WHERE q.active = true AND q.id IN " +
           "(SELECT a.question.id FROM Answer a GROUP BY a.question.id " +
           "HAVING (SUM(CASE WHEN a.isCorrect = true THEN 1.0 ELSE 0.0 END) / COUNT(a)) < :threshold)")
    List<Question> findHardestQuestions(@Param("threshold") Double threshold);
    
    // Find questions with time limits
    @Query("SELECT q FROM Question q WHERE q.timeLimit IS NOT NULL AND q.active = true")
    List<Question> findQuestionsWithTimeLimit();
    
    // Get average difficulty for quiz
    @Query("SELECT AVG(CASE " +
           "WHEN q.difficultyLevel = 'EASY' THEN 1 " +
           "WHEN q.difficultyLevel = 'MEDIUM' THEN 2 " +
           "WHEN q.difficultyLevel = 'HARD' THEN 3 " +
           "ELSE 2 END) " +
           "FROM Question q WHERE q.quizId = :quizId AND q.active = true")
    Double getAverageDifficultyForQuiz(@Param("quizId") Long quizId);
    
    // Find questions needing review (frequently answered incorrectly)
    @Query("SELECT q, COUNT(a) as wrongAnswers FROM Question q " +
           "JOIN Answer a ON a.question.id = q.id " +
           "WHERE a.isCorrect = false AND q.active = true " +
           "GROUP BY q ORDER BY wrongAnswers DESC")
    List<Object[]> findQuestionsNeedingReview();
    
    // Find next question in order
    @Query("SELECT q FROM Question q WHERE q.quizId = :quizId AND q.active = true " +
           "AND q.questionOrder > :currentOrder ORDER BY q.questionOrder ASC")
    List<Question> findNextQuestionInOrder(@Param("quizId") Long quizId, @Param("currentOrder") Integer currentOrder);
    
    // Find previous question in order
    @Query("SELECT q FROM Question q WHERE q.quizId = :quizId AND q.active = true " +
           "AND q.questionOrder < :currentOrder ORDER BY q.questionOrder DESC")
    List<Question> findPreviousQuestionInOrder(@Param("quizId") Long quizId, @Param("currentOrder") Integer currentOrder);
}
