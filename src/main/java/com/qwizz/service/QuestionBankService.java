package com.qwizz.service;

import com.qwizz.model.*;
import com.qwizz.repository.QuestionRepository;
import com.qwizz.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Microservice-style Question Bank Service
 * Handles all question-related operations including creation, management, and retrieval
 */
@Service
@Transactional
public class QuestionBankService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizRepository quizRepository;

    // Create new question
    public Question createQuestion(Question question) {
        validateQuestion(question);
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        question.setActive(true);
        return questionRepository.save(question);
    }

    // Create multiple questions for a quiz
    public List<Question> createQuestions(List<Question> questions) {
        for (Question question : questions) {
            validateQuestion(question);
            question.setCreatedAt(LocalDateTime.now());
            question.setUpdatedAt(LocalDateTime.now());
            question.setActive(true);
        }
        return questionRepository.saveAll(questions);
    }

    // Get questions by quiz ID
    public List<Question> getQuestionsByQuizId(Long quizId) {
        return questionRepository.findByQuizIdAndActiveTrue(quizId);
    }

    // Get question by ID
    public Optional<Question> getQuestionById(Long questionId) {
        return questionRepository.findById(questionId);
    }

    // Update question
    public Question updateQuestion(Long questionId, Question updatedQuestion) {
        Optional<Question> existingQuestionOpt = questionRepository.findById(questionId);
        if (existingQuestionOpt.isPresent()) {
            Question existingQuestion = existingQuestionOpt.get();
            
            existingQuestion.setQuestionText(updatedQuestion.getQuestionText());
            existingQuestion.setQuestionType(updatedQuestion.getQuestionType());
            existingQuestion.setCorrectAnswer(updatedQuestion.getCorrectAnswer());
            existingQuestion.setOptions(updatedQuestion.getOptions());
            existingQuestion.setPoints(updatedQuestion.getPoints());
            existingQuestion.setUpdatedAt(LocalDateTime.now());
            
            return questionRepository.save(existingQuestion);
        }
        throw new RuntimeException("Question not found with ID: " + questionId);
    }

    // Delete question (soft delete)
    public void deleteQuestion(Long questionId) {
        Optional<Question> questionOpt = questionRepository.findById(questionId);
        if (questionOpt.isPresent()) {
            Question question = questionOpt.get();
            question.setActive(false);
            question.setUpdatedAt(LocalDateTime.now());
            questionRepository.save(question);
        } else {
            throw new RuntimeException("Question not found with ID: " + questionId);
        }
    }

    // Get questions by type
    public List<Question> getQuestionsByType(QuestionType questionType) {
        return questionRepository.findByQuestionTypeAndActiveTrue(questionType);
    }

    // Get questions by difficulty (through quiz)
    public List<Question> getQuestionsByDifficulty(Difficulty difficulty) {
        return questionRepository.findByQuizDifficultyAndActiveTrue(difficulty);
    }

    // Search questions by text
    public List<Question> searchQuestions(String searchText) {
        return questionRepository.findByQuestionTextContainingIgnoreCaseAndActiveTrue(searchText);
    }

    // Get random questions for practice
    public List<Question> getRandomQuestions(int count) {
        Pageable pageable = PageRequest.of(0, count);
        return questionRepository.findRandomQuestions(pageable);
    }

    // Get questions by creator (teacher)
    public List<Question> getQuestionsByCreator(Long creatorId) {
        return questionRepository.findByQuizCreatorIdAndActiveTrue(creatorId);
    }

    // Bulk import questions
    public List<Question> bulkImportQuestions(Long quizId, List<Question> questions) {
        Optional<Quiz> quizOpt = quizRepository.findById(quizId);
        if (quizOpt.isEmpty()) {
            throw new RuntimeException("Quiz not found with ID: " + quizId);
        }

        for (Question question : questions) {
            question.setQuizId(quizId);
            validateQuestion(question);
            question.setCreatedAt(LocalDateTime.now());
            question.setUpdatedAt(LocalDateTime.now());
            question.setActive(true);
        }

        return questionRepository.saveAll(questions);
    }

    // Validate question data
    private void validateQuestion(Question question) {
        if (question.getQuestionText() == null || question.getQuestionText().trim().isEmpty()) {
            throw new RuntimeException("Question text cannot be empty");
        }

        if (question.getCorrectAnswer() == null || question.getCorrectAnswer().trim().isEmpty()) {
            throw new RuntimeException("Correct answer cannot be empty");
        }

        if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
            if (question.getOptions() == null || question.getOptions().trim().isEmpty()) {
                throw new RuntimeException("Multiple choice questions must have options");
            }
            
            List<String> optionsList = question.getOptionsList();
            if (optionsList.size() < 2) {
                throw new RuntimeException("Multiple choice questions must have at least 2 options");
            }
            
            if (!optionsList.contains(question.getCorrectAnswer())) {
                throw new RuntimeException("Correct answer must be one of the provided options");
            }
        }

        if (question.getQuestionType() == QuestionType.TRUE_FALSE) {
            String correctAnswer = question.getCorrectAnswer().toLowerCase();
            if (!correctAnswer.equals("true") && !correctAnswer.equals("false")) {
                throw new RuntimeException("True/False questions must have 'True' or 'False' as the correct answer");
            }
        }

        if (question.getPoints() <= 0) {
            throw new RuntimeException("Question points must be greater than 0");
        }
    }

    // Get question statistics
    public QuestionStats getQuestionStats(Long questionId) {
        // This would typically involve analyzing quiz attempts to get statistics
        // For now, return basic stats
        return new QuestionStats(questionId, 0, 0, 0.0);
    }

    // Inner class for question statistics
    public static class QuestionStats {
        private Long questionId;
        private int totalAttempts;
        private int correctAttempts;
        private double difficultyRating;

        public QuestionStats(Long questionId, int totalAttempts, int correctAttempts, double difficultyRating) {
            this.questionId = questionId;
            this.totalAttempts = totalAttempts;
            this.correctAttempts = correctAttempts;
            this.difficultyRating = difficultyRating;
        }

        // Getters
        public Long getQuestionId() { return questionId; }
        public int getTotalAttempts() { return totalAttempts; }
        public int getCorrectAttempts() { return correctAttempts; }
        public double getDifficultyRating() { return difficultyRating; }
        public double getSuccessRate() { 
            return totalAttempts > 0 ? (double) correctAttempts / totalAttempts * 100 : 0.0; 
        }
    }
}
