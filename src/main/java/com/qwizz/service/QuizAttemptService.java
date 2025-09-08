package com.qwizz.service;

import com.qwizz.model.QuizAttempt;
import com.qwizz.model.Quiz;
import com.qwizz.model.Question;
import com.qwizz.model.AttemptStatus;
import com.qwizz.repository.QuizAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class QuizAttemptService {

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private QuizService quizService;

    public QuizAttempt startQuizAttempt(Long userId, Long quizId) {
        // Check if there's already an active attempt
        Optional<QuizAttempt> activeAttempt = quizAttemptRepository.findActiveAttemptByUserIdAndQuizId(userId, quizId);
        if (activeAttempt.isPresent()) {
            return activeAttempt.get();
        }

        // Get quiz details to initialize the attempt
        Optional<Quiz> quizOpt = quizService.findById(quizId);
        if (quizOpt.isEmpty()) {
            throw new RuntimeException("Quiz not found");
        }

        Quiz quiz = quizOpt.get();
        List<Question> questions = quizService.getQuestionsByQuizId(quizId);

        QuizAttempt attempt = new QuizAttempt(userId, quizId, questions.size());
        return quizAttemptRepository.save(attempt);
    }

    public QuizAttempt submitQuizAttempt(Long attemptId, int correctAnswers, int score) {
        Optional<QuizAttempt> attemptOpt = quizAttemptRepository.findById(attemptId);
        if (attemptOpt.isEmpty()) {
            throw new RuntimeException("Quiz attempt not found");
        }

        QuizAttempt attempt = attemptOpt.get();
        attempt.setCorrectAnswers(correctAnswers);
        attempt.setScore(score);
        attempt.setEndTime(LocalDateTime.now());
        attempt.setCompleted(true);
        attempt.setStatus(AttemptStatus.COMPLETED);

        return quizAttemptRepository.save(attempt);
    }

    public QuizAttempt abandonQuizAttempt(Long attemptId) {
        Optional<QuizAttempt> attemptOpt = quizAttemptRepository.findById(attemptId);
        if (attemptOpt.isEmpty()) {
            throw new RuntimeException("Quiz attempt not found");
        }

        QuizAttempt attempt = attemptOpt.get();
        attempt.setEndTime(LocalDateTime.now());
        attempt.setCompleted(false);
        attempt.setStatus(AttemptStatus.ABANDONED);

        return quizAttemptRepository.save(attempt);
    }

    public Optional<QuizAttempt> findById(Long id) {
        return quizAttemptRepository.findById(id);
    }

    public List<QuizAttempt> getUserAttempts(Long userId) {
        return quizAttemptRepository.findByUser_IdOrderByStartTimeDesc(userId);
    }

    public List<QuizAttempt> getQuizAttempts(Long quizId) {
        return quizAttemptRepository.findByQuiz_IdOrderByStartTimeDesc(quizId);
    }

    public List<QuizAttempt> getUserQuizAttempts(Long userId, Long quizId) {
        return quizAttemptRepository.findByUser_IdAndQuiz_IdOrderByStartTimeDesc(userId, quizId);
    }

    public Optional<QuizAttempt> getActiveAttempt(Long userId, Long quizId) {
        return quizAttemptRepository.findActiveAttemptByUserIdAndQuizId(userId, quizId);
    }

    public boolean hasUserAttemptedQuiz(Long userId, Long quizId) {
        List<QuizAttempt> attempts = getUserQuizAttempts(userId, quizId);
        return !attempts.isEmpty();
    }

    public QuizAttempt getBestAttempt(Long userId, Long quizId) {
        List<QuizAttempt> attempts = getUserQuizAttempts(userId, quizId);
        return attempts.stream()
                .filter(QuizAttempt::isCompleted)
                .max((a1, a2) -> Integer.compare(a1.getScore(), a2.getScore()))
                .orElse(null);
    }

    public double getAverageScore(Long userId) {
        List<QuizAttempt> attempts = getUserAttempts(userId);
        return attempts.stream()
                .filter(QuizAttempt::isCompleted)
                .mapToDouble(QuizAttempt::getPercentage)
                .average()
                .orElse(0.0);
    }

    public long getTotalAttempts(Long userId) {
        return quizAttemptRepository.countByUser_Id(userId);
    }

    public long getCompletedAttempts(Long userId) {
        return quizAttemptRepository.countByUser_IdAndCompletedTrue(userId);
    }

    public void deleteAttempt(Long attemptId) {
        quizAttemptRepository.deleteById(attemptId);
    }
    
    // Enhanced methods for dashboard functionality
    
    public List<QuizAttempt> getRecentAttemptsByUser(Long userId, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(30); // Last 30 days
        List<QuizAttempt> attempts = quizAttemptRepository.findByUser_IdAndStartTimeAfterOrderByStartTimeDesc(userId, since);
        return attempts.size() > limit ? attempts.subList(0, limit) : attempts;
    }
    
    public List<QuizAttempt> getAttemptsByUserSince(Long userId, LocalDateTime since) {
        return quizAttemptRepository.findByUser_IdAndStartTimeAfterOrderByStartTimeDesc(userId, since);
    }
    
    public List<QuizAttempt> getRecentAttemptsByQuizCreator(Long creatorId, int limit) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizCreatorIdOrderByStartTimeDesc(creatorId);
        return attempts.size() > limit ? attempts.subList(0, limit) : attempts;
    }
    
    public Optional<QuizAttempt> getBestAttemptByUserAndQuiz(Long userId, Long quizId) {
        return quizAttemptRepository.findBestAttemptByUserAndQuiz(userId, quizId);
    }
    
    public Double getAverageScoreByUser(Long userId) {
        return quizAttemptRepository.getAverageScoreByUser(userId);
    }
    
    public Double getCompletionRateByUser(Long userId) {
        return quizAttemptRepository.getCompletionRateByUser(userId);
    }
    
    public List<QuizAttempt> getAttemptsInTimeRange(LocalDateTime start, LocalDateTime end) {
        return quizAttemptRepository.findByStartTimeBetweenOrderByStartTimeDesc(start, end);
    }
    
    public List<QuizAttempt> getHighScoreAttempts(Double minPercentage, int limit) {
        List<QuizAttempt> attempts = quizAttemptRepository.findHighScoreAttempts(minPercentage);
        return attempts.size() > limit ? attempts.subList(0, limit) : attempts;
    }
    
    public Object[] getQuizStatistics(Long quizId) {
        return quizAttemptRepository.getQuizStatistics(quizId);
    }
    
    public List<QuizAttempt> getFastestCompletions(int limit) {
        List<QuizAttempt> attempts = quizAttemptRepository.findFastestCompletions();
        return attempts.size() > limit ? attempts.subList(0, limit) : attempts;
    }
    
    public Integer getUserRankInQuiz(Long userId, Long quizId) {
        return quizAttemptRepository.findUserRankInQuiz(userId, quizId);
    }
    
    public int countCompletedAttemptsByCategory(Long categoryId) {
        return quizAttemptRepository.countCompletedAttemptsByCategory(categoryId);
    }
    
    public List<QuizAttempt> getQuizAttemptsForAnalytics(Long quizId, LocalDateTime since) {
        return quizAttemptRepository.findQuizAttemptsForAnalytics(quizId, since);
    }
    
    public List<QuizAttempt> getCompletedAttemptsByUserOrderByEndTime(Long userId) {
        return quizAttemptRepository.findCompletedAttemptsByUserOrderByEndTime(userId);
    }
    
    public int getTotalAttemptsForCreatedQuizzes(Long creatorId) {
        return quizAttemptRepository.countAttemptsByCreatorId(creatorId);
    }
}
