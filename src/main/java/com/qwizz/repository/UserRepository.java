package com.qwizz.repository;

import com.qwizz.model.Role;
import com.qwizz.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(Role role);
    
    List<User> findByActiveTrue();
    
    List<User> findByRoleAndActiveTrue(Role role);
    
    // Additional methods for enhanced features
    
    // Find top users by points
    List<User> findTop10ByOrderByTotalPointsDesc();
    
    // Find users by quiz streak
    List<User> findByQuizStreakGreaterThanOrderByQuizStreakDesc(Integer minStreak);
    
    // Find users by average score
    @Query("SELECT u FROM User u WHERE u.averageScore >= :minScore ORDER BY u.averageScore DESC")
    List<User> findByAverageScoreGreaterThanEqual(Double minScore);
    
    // Find most active users
    @Query("SELECT u FROM User u ORDER BY u.totalQuizzesTaken DESC")
    List<User> findMostActiveUsers();
    
    // Find top creators
    @Query("SELECT u FROM User u ORDER BY u.totalQuizzesCreated DESC")
    List<User> findTopCreators();
    
    // Search users by name
    @Query("SELECT u FROM User u WHERE u.active = true AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> searchUsers(String search);
    
    // Find users by location
    List<User> findByLocationContainingIgnoreCaseAndActiveTrue(String location);
    
    // Count users by role
    int countByRole(Role role);
    
    // Find users who joined recently
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<User> findRecentUsers(java.time.LocalDateTime since);
}
