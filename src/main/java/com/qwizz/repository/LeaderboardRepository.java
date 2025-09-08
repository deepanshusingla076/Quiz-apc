package com.qwizz.repository;

import com.qwizz.model.Leaderboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, Long> {
    
    // Find global leaderboard
    List<Leaderboard> findByTypeOrderByScoreValueDesc(Leaderboard.LeaderboardType type);
    
    // Find leaderboard for specific quiz
    List<Leaderboard> findByQuizIdAndTypeOrderByScoreValueDesc(Long quizId, Leaderboard.LeaderboardType type);
    
    // Find leaderboard for category
    List<Leaderboard> findByCategoryIdAndTypeOrderByScoreValueDesc(Long categoryId, Leaderboard.LeaderboardType type);
    
    // Find user's rank in global leaderboard
    @Query("SELECT COUNT(l) + 1 FROM Leaderboard l WHERE l.type = :type AND l.scoreValue > " +
           "(SELECT l2.scoreValue FROM Leaderboard l2 WHERE l2.user.id = :userId AND l2.type = :type)")
    Integer findUserRank(Long userId, Leaderboard.LeaderboardType type);
    
    // Find top N users for type
    @Query("SELECT l FROM Leaderboard l WHERE l.type = :type ORDER BY l.scoreValue DESC")
    List<Leaderboard> findTopUsers(Leaderboard.LeaderboardType type);
    
    // Find leaderboard entries in time period
    List<Leaderboard> findByTypeAndPeriodStartAfterAndPeriodEndBeforeOrderByScoreValueDesc(
        Leaderboard.LeaderboardType type, LocalDateTime periodStart, LocalDateTime periodEnd);
    
    // Find user's entry for specific type and period
    @Query("SELECT l FROM Leaderboard l WHERE l.user.id = :userId AND l.type = :type " +
           "AND (:quizId IS NULL OR l.quiz.id = :quizId) " +
           "AND (:categoryId IS NULL OR l.category.id = :categoryId)")
    List<Leaderboard> findUserEntries(Long userId, Leaderboard.LeaderboardType type, Long quizId, Long categoryId);
}
