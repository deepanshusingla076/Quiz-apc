package com.qwizz.repository;

import com.qwizz.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    
    // Find user's achievements
    List<UserAchievement> findByUserIdOrderByEarnedAtDesc(Long userId);
    
    // Find recent achievements
    List<UserAchievement> findByUserIdAndEarnedAtAfterOrderByEarnedAtDesc(Long userId, LocalDateTime since);
    
    // Check if user has specific achievement
    Optional<UserAchievement> findByUserIdAndAchievementId(Long userId, Long achievementId);
    
    // Count user's achievements
    int countByUserId(Long userId);
    
    // Find top achievers
    @Query("SELECT ua.user.id, COUNT(ua) as achievementCount FROM UserAchievement ua " +
           "GROUP BY ua.user.id ORDER BY achievementCount DESC")
    List<Object[]> findTopAchievers();
    
    // Find users with specific achievement
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.achievement.id = :achievementId ORDER BY ua.earnedAt ASC")
    List<UserAchievement> findUsersWithAchievement(Long achievementId);
}
