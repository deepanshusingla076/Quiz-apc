package com.qwizz.repository;

import com.qwizz.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Find active categories
    List<Category> findByIsActiveTrue();
    
    // Find category by name
    Optional<Category> findByName(String name);
    
    // Find categories with active quizzes
    @Query("SELECT DISTINCT c FROM Category c JOIN c.quizzes q WHERE c.isActive = true AND q.active = true")
    List<Category> findCategoriesWithActiveQuizzes();
    
    // Count quizzes in category
    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.category.id = :categoryId AND q.active = true")
    int countActiveQuizzesByCategory(Long categoryId);
    
    // Find categories ordered by quiz count
    @Query("SELECT c FROM Category c LEFT JOIN c.quizzes q WHERE c.isActive = true GROUP BY c ORDER BY COUNT(q) DESC")
    List<Category> findCategoriesOrderedByQuizCount();
}
