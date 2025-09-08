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
}
