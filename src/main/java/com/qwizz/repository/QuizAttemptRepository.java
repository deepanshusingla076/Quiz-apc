package com.qwizz.repository;

import com.qwizz.model.AttemptStatus;
import com.qwizz.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
