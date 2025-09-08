package com.qwizz.repository;

import com.qwizz.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    
    // Find active achievements
    List<Achievement> findByIsActiveTrue();
    
    // Find achievements by type
    List<Achievement> findByTypeAndIsActiveTrue(Achievement.AchievementType type);
    
    // Find achievements not earned by user
    @Query("SELECT a FROM Achievement a WHERE a.isActive = true AND a.id NOT IN " +
           "(SELECT ua.achievement.id FROM UserAchievement ua WHERE ua.user.id = :userId)")
    List<Achievement> findNotEarnedByUser(Long userId);
    
    // Find achievements that user might be close to earning
    @Query("SELECT a FROM Achievement a WHERE a.isActive = true AND a.id NOT IN " +
           "(SELECT ua.achievement.id FROM UserAchievement ua WHERE ua.user.id = :userId) " +
           "AND a.type IN :types")
    List<Achievement> findPotentialAchievements(Long userId, List<Achievement.AchievementType> types);
}
