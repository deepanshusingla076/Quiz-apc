package com.qwizz.service;

import com.qwizz.model.Quiz;
import com.qwizz.model.Question;
import com.qwizz.model.Difficulty;
import com.qwizz.repository.QuizRepository;
import com.qwizz.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    public Quiz createQuiz(Quiz quiz) {
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setUpdatedAt(LocalDateTime.now());
        quiz.setActive(true);
        return quizRepository.save(quiz);
    }

    public Quiz updateQuiz(Quiz quiz) {
        quiz.setUpdatedAt(LocalDateTime.now());
        return quizRepository.save(quiz);
    }

    public Optional<Quiz> findById(Long id) {
        return quizRepository.findById(id);
    }

    public Optional<Quiz> getQuizById(Long id) {
        return quizRepository.findById(id);
    }

    public List<Quiz> getAllQuizzes() {
        return quizRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    public List<Quiz> getPublicQuizzes() {
        return quizRepository.findByIsPublicTrueAndActiveTrue();
    }

    public List<Quiz> getQuizzesByCreator(Long creatorId) {
        return quizRepository.findByCreatorIdAndActiveTrue(creatorId);
    }

    public List<Quiz> getQuizzesByDifficulty(Difficulty difficulty) {
        return quizRepository.findByDifficultyAndIsPublicTrueAndActiveTrue(difficulty);
    }

    public List<Quiz> searchQuizzes(String searchTerm) {
        return quizRepository.searchByTitle(searchTerm);
    }

    public void deleteQuiz(Long quizId) {
        Optional<Quiz> quizOpt = quizRepository.findById(quizId);
        if (quizOpt.isPresent()) {
            Quiz quiz = quizOpt.get();
            quiz.setActive(false);
            quiz.setUpdatedAt(LocalDateTime.now());
            quizRepository.save(quiz);
            
            // Soft delete questions as well
            List<Question> questions = questionRepository.findByQuizIdAndActiveTrue(quizId);
            for (Question question : questions) {
                question.setActive(false);
                question.setUpdatedAt(LocalDateTime.now());
                questionRepository.save(question);
            }
        }
    }

    public Question addQuestionToQuiz(Long quizId, Question question) {
        question.setQuizId(quizId);
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        question.setActive(true);
        return questionRepository.save(question);
    }

    public Question updateQuestion(Question question) {
        question.setUpdatedAt(LocalDateTime.now());
        return questionRepository.save(question);
    }

    public Optional<Question> findQuestionById(Long questionId) {
        return questionRepository.findById(questionId);
    }

    public List<Question> getQuestionsByQuizId(Long quizId) {
        return questionRepository.findByQuizIdAndActiveTrue(quizId);
    }

    public void deleteQuestion(Long questionId) {
        Optional<Question> questionOpt = questionRepository.findById(questionId);
        if (questionOpt.isPresent()) {
            Question question = questionOpt.get();
            question.setActive(false);
            question.setUpdatedAt(LocalDateTime.now());
            questionRepository.save(question);
        }
    }

    public long getQuizCount(Long creatorId) {
        return quizRepository.countByCreatorIdAndActiveTrue(creatorId);
    }

    public long getQuestionCount(Long quizId) {
        return questionRepository.countByQuizIdAndActiveTrue(quizId);
    }

    public Quiz generateAIQuiz(String topic, Difficulty difficulty, int questionCount, Long creatorId) {
        // This is a placeholder for AI integration
        // In a real implementation, you would integrate with an AI service

        Quiz quiz = new Quiz();
        quiz.setTitle("AI Generated Quiz: " + topic);
        quiz.setDescription("AI generated quiz about " + topic + " with " + difficulty + " difficulty");
        quiz.setCreatorId(creatorId);
        quiz.setDifficulty(difficulty);
        quiz.setTimeLimit(questionCount * 2); // 2 minutes per question
        quiz.setPublic(true);
        quiz.setAiGenerated(true);

        Quiz savedQuiz = createQuiz(quiz);

        // Generate sample questions (in real implementation, this would come from AI)
        for (int i = 1; i <= questionCount; i++) {
            Question question = new Question();
            question.setQuizId(savedQuiz.getId());
            question.setQuestionText("Sample AI generated question " + i + " about " + topic);
            question.setQuestionType(com.qwizz.model.QuestionType.MULTIPLE_CHOICE);
            question.setCorrectAnswer("Option A");
            question.setOptionsList(List.of("Option A", "Option B", "Option C", "Option D"));
            question.setPoints(10);
            addQuestionToQuiz(savedQuiz.getId(), question);
        }

        return findById(savedQuiz.getId()).orElse(savedQuiz);
    }

    public boolean canUserEditQuiz(Long quizId, Long userId) {
        Optional<Quiz> quizOpt = findById(quizId);
        return quizOpt.isPresent() && quizOpt.get().getCreatorId().equals(userId);
    }
    
    // Enhanced methods for dashboard functionality
    
    public List<Quiz> getQuizzesByCategory(Long categoryId, int limit) {
        List<Quiz> quizzes = quizRepository.findByCategoryIdAndIsPublicTrueAndActiveTrueOrderByCreatedAtDesc(categoryId);
        return quizzes.size() > limit ? quizzes.subList(0, limit) : quizzes;
    }
    
    public List<Quiz> getQuizzesByCategory(Long categoryId) {
        return quizRepository.findByCategoryIdAndIsPublicTrueAndActiveTrueOrderByCreatedAtDesc(categoryId);
    }
    
    public List<Quiz> getSuggestedQuizzes(Long userId, int limit) {
        List<Quiz> quizzes = quizRepository.findQuizzesNotAttemptedByUser(userId);
        return quizzes.size() > limit ? quizzes.subList(0, limit) : quizzes;
    }
    
    public List<Quiz> getPopularQuizzes() {
        return quizRepository.findPopularQuizzes();
    }
    
    public List<Quiz> getHighestRatedQuizzes() {
        return quizRepository.findHighestRatedQuizzes();
    }
    
    public List<Quiz> getAIGeneratedQuizzes() {
        return quizRepository.findByAiGeneratedTrueAndIsPublicTrueAndActiveTrue();
    }
    
    public List<Quiz> getRecentQuizzes(LocalDateTime since) {
        return quizRepository.findRecentQuizzes(since);
    }
    
    public List<Quiz> searchQuizzesWithFilters(Long categoryId, Difficulty difficulty, String searchTerm) {
        return quizRepository.findQuizzesWithFilters(categoryId, difficulty, searchTerm);
    }
    
    public List<Quiz> getSimilarQuizzes(Quiz quiz, int limit) {
        if (quiz.getCategory() == null) {
            return List.of();
        }
        
        int difficultyValue = quiz.getDifficulty().ordinal() + 1;
        List<Quiz> similarQuizzes = quizRepository.findSimilarQuizzes(
            quiz.getId(), 
            quiz.getCategory().getId(), 
            difficultyValue
        );
        
        return similarQuizzes.size() > limit ? similarQuizzes.subList(0, limit) : similarQuizzes;
    }
    
    public int getQuizCountByCategory(Long categoryId) {
        return quizRepository.countByCategoryId(categoryId);
    }
    
    public void updateQuizStatistics(Quiz quiz, double newScore) {
        quiz.incrementTotalAttempts();
        quiz.updateAverageScore(newScore);
        quizRepository.save(quiz);
    }
    
    public String findMostPopularQuizId(Long creatorId) {
        List<Quiz> createdQuizzes = getQuizzesByCreator(creatorId);
        if (createdQuizzes.isEmpty()) {
            return "N/A";
        }
        
        Quiz mostPopular = createdQuizzes.stream()
                .max((q1, q2) -> {
                    int attempts1 = q1.getTotalAttempts() != null ? q1.getTotalAttempts() : 0;
                    int attempts2 = q2.getTotalAttempts() != null ? q2.getTotalAttempts() : 0;
                    return Integer.compare(attempts1, attempts2);
                })
                .orElse(null);
        
        return mostPopular != null ? mostPopular.getTitle() : "N/A";
    }
}
