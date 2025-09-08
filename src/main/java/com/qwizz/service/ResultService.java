package com.qwizz.service;

import com.qwizz.model.*;
import com.qwizz.repository.QuizAttemptRepository;
import com.qwizz.repository.QuizRepository;
import com.qwizz.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Microservice-style Result Service
 * Handles all result processing, analytics, and reporting operations
 */
@Service
@Transactional
public class ResultService {

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserRepository userRepository;

    // Start a new quiz attempt
    public QuizAttempt startQuizAttempt(Long userId, Long quizId) {
        // Check if there's an active attempt
        Optional<QuizAttempt> activeAttempt = quizAttemptRepository
                .findActiveAttemptByUserIdAndQuizId(userId, quizId);
        
        if (activeAttempt.isPresent()) {
            return activeAttempt.get(); // Return existing active attempt
        }

        // Get quiz to determine total questions
        Optional<Quiz> quizOpt = quizRepository.findById(quizId);
        if (quizOpt.isEmpty()) {
            throw new RuntimeException("Quiz not found with ID: " + quizId);
        }

        Quiz quiz = quizOpt.get();
        QuizAttempt attempt = new QuizAttempt(userId, quizId, quiz.getQuestionCount());
        return quizAttemptRepository.save(attempt);
    }

    // Submit quiz answers and calculate results
    public QuizAttempt submitQuizAttempt(Long attemptId, Map<Long, String> answers) {
        Optional<QuizAttempt> attemptOpt = quizAttemptRepository.findById(attemptId);
        if (attemptOpt.isEmpty()) {
            throw new RuntimeException("Quiz attempt not found with ID: " + attemptId);
        }

        QuizAttempt attempt = attemptOpt.get();
        if (attempt.isCompleted()) {
            throw new RuntimeException("Quiz attempt is already completed");
        }

        // Get quiz and questions
        Optional<Quiz> quizOpt = quizRepository.findById(attempt.getQuizId());
        if (quizOpt.isEmpty()) {
            throw new RuntimeException("Quiz not found");
        }

        Quiz quiz = quizOpt.get();
        List<Question> questions = quiz.getQuestions();

        // Calculate score
        int correctAnswers = 0;
        int totalScore = 0;

        for (Question question : questions) {
            String userAnswer = answers.get(question.getId());
            if (userAnswer != null && isAnswerCorrect(question, userAnswer)) {
                correctAnswers++;
                totalScore += question.getPoints();
            }
        }

        // Update attempt
        attempt.setCorrectAnswers(correctAnswers);
        attempt.setScore(totalScore);
        attempt.setCompleted(true);
        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus(AttemptStatus.COMPLETED);

        return quizAttemptRepository.save(attempt);
    }

    // Get user's quiz attempts
    public List<QuizAttempt> getUserAttempts(Long userId) {
        return quizAttemptRepository.findByUser_IdOrderByStartTimeDesc(userId);
    }

    // Get attempts for a specific quiz
    public List<QuizAttempt> getQuizAttempts(Long quizId) {
        return quizAttemptRepository.findByQuiz_IdOrderByStartTimeDesc(quizId);
    }

    // Get attempts for quizzes created by a teacher
    public List<QuizAttempt> getTeacherQuizAttempts(Long teacherId) {
        return quizAttemptRepository.findByQuizCreatorIdOrderByStartTimeDesc(teacherId);
    }

    // Get detailed result analytics for a quiz
    public QuizAnalytics getQuizAnalytics(Long quizId) {
        Optional<Quiz> quizOpt = quizRepository.findById(quizId);
        if (quizOpt.isEmpty()) {
            throw new RuntimeException("Quiz not found with ID: " + quizId);
        }

        Quiz quiz = quizOpt.get();
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuiz_IdOrderByStartTimeDesc(quizId);
        List<QuizAttempt> completedAttempts = attempts.stream()
                .filter(QuizAttempt::isCompleted)
                .collect(Collectors.toList());

        return new QuizAnalytics(quiz, completedAttempts);
    }

    // Get student performance analytics
    public StudentAnalytics getStudentAnalytics(Long studentId) {
        Optional<User> userOpt = userRepository.findById(studentId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Student not found with ID: " + studentId);
        }

        User student = userOpt.get();
        List<QuizAttempt> attempts = quizAttemptRepository.findByUser_IdOrderByStartTimeDesc(studentId);
        List<QuizAttempt> completedAttempts = attempts.stream()
                .filter(QuizAttempt::isCompleted)
                .collect(Collectors.toList());

        return new StudentAnalytics(student, completedAttempts);
    }

    // Get teacher dashboard analytics
    public TeacherAnalytics getTeacherAnalytics(Long teacherId) {
        Optional<User> userOpt = userRepository.findById(teacherId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Teacher not found with ID: " + teacherId);
        }

        User teacher = userOpt.get();
        List<Quiz> quizzes = quizRepository.findByCreatorIdAndActiveTrue(teacherId);
        List<QuizAttempt> allAttempts = quizAttemptRepository.findByQuizCreatorIdOrderByStartTimeDesc(teacherId);

        return new TeacherAnalytics(teacher, quizzes, allAttempts);
    }

    // Get leaderboard for a quiz
    public List<LeaderboardEntry> getQuizLeaderboard(Long quizId, int limit) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuiz_IdOrderByStartTimeDesc(quizId);
        
        Map<Long, QuizAttempt> bestAttempts = new HashMap<>();
        for (QuizAttempt attempt : attempts) {
            if (attempt.isCompleted()) {
                Long userId = attempt.getUserId();
                QuizAttempt bestAttempt = bestAttempts.get(userId);
                if (bestAttempt == null || attempt.getScore() > bestAttempt.getScore()) {
                    bestAttempts.put(userId, attempt);
                }
            }
        }

        return bestAttempts.values().stream()
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                .limit(limit)
                .map(attempt -> {
                    Optional<User> userOpt = userRepository.findById(attempt.getUserId());
                    String studentName = userOpt.map(User::getFullName).orElse("Unknown");
                    return new LeaderboardEntry(studentName, attempt.getScore(), 
                            attempt.getPercentage(), attempt.getFormattedDuration());
                })
                .collect(Collectors.toList());
    }

    // Helper method to check if answer is correct
    private boolean isAnswerCorrect(Question question, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return false;
        }

        String correctAnswer = question.getCorrectAnswer().trim();
        String providedAnswer = userAnswer.trim();

        switch (question.getQuestionType()) {
            case MULTIPLE_CHOICE:
            case TRUE_FALSE:
                return correctAnswer.equalsIgnoreCase(providedAnswer);
            case SHORT_ANSWER:
                // For short answers, allow some flexibility in matching
                return correctAnswer.equalsIgnoreCase(providedAnswer) ||
                       correctAnswer.toLowerCase().contains(providedAnswer.toLowerCase()) ||
                       providedAnswer.toLowerCase().contains(correctAnswer.toLowerCase());
            default:
                return false;
        }
    }

    // Analytics classes
    public static class QuizAnalytics {
        private Quiz quiz;
        private int totalAttempts;
        private int completedAttempts;
        private double averageScore;
        private double averagePercentage;
        private int highestScore;
        private int lowestScore;
        private Map<String, Integer> difficultyDistribution;

        public QuizAnalytics(Quiz quiz, List<QuizAttempt> completedAttempts) {
            this.quiz = quiz;
            this.totalAttempts = completedAttempts.size();
            this.completedAttempts = completedAttempts.size();
            
            if (!completedAttempts.isEmpty()) {
                this.averageScore = completedAttempts.stream().mapToInt(QuizAttempt::getScore).average().orElse(0.0);
                this.averagePercentage = completedAttempts.stream().mapToDouble(QuizAttempt::getPercentage).average().orElse(0.0);
                this.highestScore = completedAttempts.stream().mapToInt(QuizAttempt::getScore).max().orElse(0);
                this.lowestScore = completedAttempts.stream().mapToInt(QuizAttempt::getScore).min().orElse(0);
            }
            
            this.difficultyDistribution = calculateDifficultyDistribution(completedAttempts);
        }

        private Map<String, Integer> calculateDifficultyDistribution(List<QuizAttempt> attempts) {
            Map<String, Integer> distribution = new HashMap<>();
            distribution.put("Easy", 0);
            distribution.put("Medium", 0);
            distribution.put("Hard", 0);
            
            for (QuizAttempt attempt : attempts) {
                double percentage = attempt.getPercentage();
                if (percentage >= 80) distribution.put("Easy", distribution.get("Easy") + 1);
                else if (percentage >= 60) distribution.put("Medium", distribution.get("Medium") + 1);
                else distribution.put("Hard", distribution.get("Hard") + 1);
            }
            
            return distribution;
        }

        // Getters
        public Quiz getQuiz() { return quiz; }
        public int getTotalAttempts() { return totalAttempts; }
        public int getCompletedAttempts() { return completedAttempts; }
        public double getAverageScore() { return averageScore; }
        public double getAveragePercentage() { return averagePercentage; }
        public int getHighestScore() { return highestScore; }
        public int getLowestScore() { return lowestScore; }
        public Map<String, Integer> getDifficultyDistribution() { return difficultyDistribution; }
    }

    public static class StudentAnalytics {
        private User student;
        private int totalQuizzesTaken;
        private int quizzesCompleted;
        private double averageScore;
        private double averagePercentage;
        private int bestScore;
        private String favoriteSubject;

        public StudentAnalytics(User student, List<QuizAttempt> completedAttempts) {
            this.student = student;
            this.totalQuizzesTaken = completedAttempts.size();
            this.quizzesCompleted = completedAttempts.size();
            
            if (!completedAttempts.isEmpty()) {
                this.averageScore = completedAttempts.stream().mapToInt(QuizAttempt::getScore).average().orElse(0.0);
                this.averagePercentage = completedAttempts.stream().mapToDouble(QuizAttempt::getPercentage).average().orElse(0.0);
                this.bestScore = completedAttempts.stream().mapToInt(QuizAttempt::getScore).max().orElse(0);
            }
            
            this.favoriteSubject = "General Knowledge"; // Could be calculated based on quiz categories
        }

        // Getters
        public User getStudent() { return student; }
        public int getTotalQuizzesTaken() { return totalQuizzesTaken; }
        public int getQuizzesCompleted() { return quizzesCompleted; }
        public double getAverageScore() { return averageScore; }
        public double getAveragePercentage() { return averagePercentage; }
        public int getBestScore() { return bestScore; }
        public String getFavoriteSubject() { return favoriteSubject; }
    }

    public static class TeacherAnalytics {
        private User teacher;
        private int totalQuizzesCreated;
        private int totalStudentAttempts;
        private double averageQuizScore;
        private int mostPopularQuizId;

        public TeacherAnalytics(User teacher, List<Quiz> quizzes, List<QuizAttempt> attempts) {
            this.teacher = teacher;
            this.totalQuizzesCreated = quizzes.size();
            this.totalStudentAttempts = attempts.size();
            
            List<QuizAttempt> completedAttempts = attempts.stream()
                    .filter(QuizAttempt::isCompleted)
                    .collect(Collectors.toList());
            
            if (!completedAttempts.isEmpty()) {
                this.averageQuizScore = completedAttempts.stream()
                        .mapToDouble(QuizAttempt::getPercentage)
                        .average().orElse(0.0);
            }
            
            // Find most popular quiz (most attempts)
            this.mostPopularQuizId = attempts.stream()
                    .collect(Collectors.groupingBy(QuizAttempt::getQuizId, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(0L).intValue();
        }

        // Getters
        public User getTeacher() { return teacher; }
        public int getTotalQuizzesCreated() { return totalQuizzesCreated; }
        public int getTotalStudentAttempts() { return totalStudentAttempts; }
        public double getAverageQuizScore() { return averageQuizScore; }
        public int getMostPopularQuizId() { return mostPopularQuizId; }
    }

    public static class LeaderboardEntry {
        private String studentName;
        private int score;
        private double percentage;
        private String duration;

        public LeaderboardEntry(String studentName, int score, double percentage, String duration) {
            this.studentName = studentName;
            this.score = score;
            this.percentage = percentage;
            this.duration = duration;
        }

        // Getters
        public String getStudentName() { return studentName; }
        public int getScore() { return score; }
        public double getPercentage() { return percentage; }
        public String getDuration() { return duration; }
    }
}
