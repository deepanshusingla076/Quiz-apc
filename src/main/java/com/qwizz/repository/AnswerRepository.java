package com.qwizz.repository;

import com.qwizz.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    
    // Find answers by quiz attempt
    List<Answer> findByQuizAttemptIdOrderByQuestionId(Long quizAttemptId);
    
    // Find answers by question
    List<Answer> findByQuestionId(Long questionId);
    
    // Find correct answers by attempt
    List<Answer> findByQuizAttemptIdAndIsCorrectTrue(Long quizAttemptId);
    
    // Find incorrect answers by attempt
    List<Answer> findByQuizAttemptIdAndIsCorrectFalse(Long quizAttemptId);
    
    // Count correct answers for attempt
    int countByQuizAttemptIdAndIsCorrectTrue(Long quizAttemptId);
    
    // Count total answers for attempt
    int countByQuizAttemptId(Long quizAttemptId);
    
    // Calculate average time taken for question
    @Query("SELECT AVG(a.timeTaken) FROM Answer a WHERE a.question.id = :questionId AND a.timeTaken IS NOT NULL")
    Double getAverageTimeForQuestion(Long questionId);
    
    // Find answers with their time taken
    @Query("SELECT a FROM Answer a WHERE a.quizAttempt.id = :attemptId AND a.timeTaken IS NOT NULL ORDER BY a.timeTaken DESC")
    List<Answer> findAnswersWithTimeByAttempt(Long attemptId);
}
