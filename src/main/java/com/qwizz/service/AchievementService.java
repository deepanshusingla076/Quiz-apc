package com.qwizz.service;

import com.qwizz.model.Achievement;
import com.qwizz.model.User;
import com.qwizz.model.UserAchievement;
import com.qwizz.repository.AchievementRepository;
import com.qwizz.repository.UserAchievementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AchievementService {
    
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    
    public AchievementService(AchievementRepository achievementRepository, 
                            UserAchievementRepository userAchievementRepository) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
    }
    
    public List<Achievement> getAllActiveAchievements() {
        return achievementRepository.findByIsActiveTrue();
    }
    
    public List<Achievement> getAchievementsByType(Achievement.AchievementType type) {
        return achievementRepository.findByTypeAndIsActiveTrue(type);
    }
    
    public List<Achievement> getNotEarnedAchievements(Long userId) {
        return achievementRepository.findNotEarnedByUser(userId);
    }
    
    public List<UserAchievement> getUserAchievements(Long userId) {
        return userAchievementRepository.findByUserIdOrderByEarnedAtDesc(userId);
    }
    
    public List<UserAchievement> getRecentUserAchievements(Long userId, LocalDateTime since) {
        return userAchievementRepository.findByUserIdAndEarnedAtAfterOrderByEarnedAtDesc(userId, since);
    }
    
    public boolean hasUserEarnedAchievement(Long userId, Long achievementId) {
        return userAchievementRepository.findByUserIdAndAchievementId(userId, achievementId).isPresent();
    }
    
    public UserAchievement awardAchievement(User user, Achievement achievement) {
        if (hasUserEarnedAchievement(user.getId(), achievement.getId())) {
            return null; // Already earned
        }
        
        UserAchievement userAchievement = new UserAchievement(user, achievement);
        UserAchievement saved = userAchievementRepository.save(userAchievement);
        
        // Award points to user
        if (achievement.getPointsReward() != null && achievement.getPointsReward() > 0) {
            user.addPoints(achievement.getPointsReward());
        }
        
        return saved;
    }
    
    public void checkAndAwardAchievements(User user) {
        // Check quiz completion achievements
        checkQuizCompletionAchievements(user);
        
        // Check streak achievements
        checkStreakAchievements(user);
        
        // Check points achievements
        checkPointsAchievements(user);
        
        // Check perfect score achievements
        checkPerfectScoreAchievements(user);
        
        // Check quiz creation achievements
        checkQuizCreationAchievements(user);
    }
    
    private void checkQuizCompletionAchievements(User user) {
        List<Achievement> completionAchievements = getAchievementsByType(Achievement.AchievementType.QUIZ_COMPLETED);
        
        for (Achievement achievement : completionAchievements) {
            if (achievement.getRequirementValue() != null && 
                user.getTotalQuizzesTaken() >= achievement.getRequirementValue() &&
                !hasUserEarnedAchievement(user.getId(), achievement.getId())) {
                awardAchievement(user, achievement);
            }
        }
    }
    
    private void checkStreakAchievements(User user) {
        List<Achievement> streakAchievements = getAchievementsByType(Achievement.AchievementType.QUIZ_STREAK);
        
        for (Achievement achievement : streakAchievements) {
            if (achievement.getRequirementValue() != null && 
                user.getQuizStreak() >= achievement.getRequirementValue() &&
                !hasUserEarnedAchievement(user.getId(), achievement.getId())) {
                awardAchievement(user, achievement);
            }
        }
    }
    
    private void checkPointsAchievements(User user) {
        List<Achievement> pointsAchievements = getAchievementsByType(Achievement.AchievementType.POINTS_EARNED);
        
        for (Achievement achievement : pointsAchievements) {
            if (achievement.getRequirementValue() != null && 
                user.getTotalPoints() >= achievement.getRequirementValue() &&
                !hasUserEarnedAchievement(user.getId(), achievement.getId())) {
                awardAchievement(user, achievement);
            }
        }
    }
    
    private void checkPerfectScoreAchievements(User user) {
        // This would require checking recent quiz attempts for perfect scores
        // Implementation depends on QuizAttempt data
    }
    
    private void checkQuizCreationAchievements(User user) {
        List<Achievement> creationAchievements = getAchievementsByType(Achievement.AchievementType.QUIZ_CREATED);
        
        for (Achievement achievement : creationAchievements) {
            if (achievement.getRequirementValue() != null && 
                user.getTotalQuizzesCreated() >= achievement.getRequirementValue() &&
                !hasUserEarnedAchievement(user.getId(), achievement.getId())) {
                awardAchievement(user, achievement);
            }
        }
    }
    
    public Achievement createAchievement(Achievement achievement) {
        return achievementRepository.save(achievement);
    }
    
    public void initializeDefaultAchievements() {
        if (achievementRepository.count() == 0) {
            // Quiz completion achievements
            createAchievement(new Achievement("First Steps", "Complete your first quiz", Achievement.AchievementType.QUIZ_COMPLETED, 1, 10));
            createAchievement(new Achievement("Getting Started", "Complete 5 quizzes", Achievement.AchievementType.QUIZ_COMPLETED, 5, 25));
            createAchievement(new Achievement("Quiz Enthusiast", "Complete 25 quizzes", Achievement.AchievementType.QUIZ_COMPLETED, 25, 100));
            createAchievement(new Achievement("Quiz Master", "Complete 100 quizzes", Achievement.AchievementType.QUIZ_COMPLETED, 100, 500));
            
            // Streak achievements
            createAchievement(new Achievement("On Fire", "Maintain a 3-day quiz streak", Achievement.AchievementType.QUIZ_STREAK, 3, 20));
            createAchievement(new Achievement("Dedicated Learner", "Maintain a 7-day quiz streak", Achievement.AchievementType.QUIZ_STREAK, 7, 50));
            createAchievement(new Achievement("Unstoppable", "Maintain a 30-day quiz streak", Achievement.AchievementType.QUIZ_STREAK, 30, 250));
            
            // Points achievements
            createAchievement(new Achievement("Point Collector", "Earn 100 points", Achievement.AchievementType.POINTS_EARNED, 100, 15));
            createAchievement(new Achievement("High Scorer", "Earn 1000 points", Achievement.AchievementType.POINTS_EARNED, 1000, 75));
            createAchievement(new Achievement("Point Master", "Earn 10000 points", Achievement.AchievementType.POINTS_EARNED, 10000, 400));
            
            // Creation achievements
            createAchievement(new Achievement("Creator", "Create your first quiz", Achievement.AchievementType.QUIZ_CREATED, 1, 30));
            createAchievement(new Achievement("Quiz Builder", "Create 5 quizzes", Achievement.AchievementType.QUIZ_CREATED, 5, 100));
            createAchievement(new Achievement("Content Master", "Create 20 quizzes", Achievement.AchievementType.QUIZ_CREATED, 20, 500));
            
            // Special achievements
            Achievement perfectScore = new Achievement("Perfectionist", "Score 100% on any quiz", Achievement.AchievementType.PERFECT_SCORE, 1, 50);
            perfectScore.setIcon("fas fa-star");
            perfectScore.setColor("#FFD700");
            createAchievement(perfectScore);
            
            Achievement speedDemon = new Achievement("Speed Demon", "Complete a quiz in under 30 seconds", Achievement.AchievementType.SPEED_DEMON, 1, 75);
            speedDemon.setIcon("fas fa-bolt");
            speedDemon.setColor("#FF6B35");
            createAchievement(speedDemon);
        }
    }
}
