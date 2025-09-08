package com.qwizz.repository;

import com.qwizz.model.Difficulty;
import com.qwizz.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByCreatorIdAndActiveTrue(Long creatorId);
    
    List<Quiz> findByIsPublicTrueAndActiveTrue();
    
    List<Quiz> findByDifficultyAndIsPublicTrueAndActiveTrue(Difficulty difficulty);
    
    @Query("SELECT q FROM Quiz q WHERE q.title LIKE %:searchTerm% AND q.isPublic = true AND q.active = true ORDER BY q.createdAt DESC")
    List<Quiz> searchByTitle(@Param("searchTerm") String searchTerm);
    
    long countByCreatorIdAndActiveTrue(Long creatorId);
    
    List<Quiz> findByActiveTrueOrderByCreatedAtDesc();
}
