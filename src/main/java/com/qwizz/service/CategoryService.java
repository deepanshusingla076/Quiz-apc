package com.qwizz.service;

import com.qwizz.model.Category;
import com.qwizz.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrue();
    }
    
    public List<Category> getCategoriesWithActiveQuizzes() {
        return categoryRepository.findCategoriesWithActiveQuizzes();
    }
    
    public List<Category> getCategoriesOrderedByQuizCount() {
        return categoryRepository.findCategoriesOrderedByQuizCount();
    }
    
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
    
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }
    
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }
    
    public Category updateCategory(Category category) {
        return categoryRepository.save(category);
    }
    
    public void deleteCategoryById(Long id) {
        categoryRepository.findById(id).ifPresent(category -> {
            category.setIsActive(false);
            categoryRepository.save(category);
        });
    }
    
    public int getQuizCountForCategory(Long categoryId) {
        return categoryRepository.countActiveQuizzesByCategory(categoryId);
    }
    
    public Category createDefaultCategory(String name, String description, String color, String icon) {
        Category category = new Category(name, description, color, icon);
        return categoryRepository.save(category);
    }
    
    public void initializeDefaultCategories() {
        if (categoryRepository.count() == 0) {
            createDefaultCategory("Science", "Questions about physics, chemistry, biology, and other sciences", "#4CAF50", "fas fa-flask");
            createDefaultCategory("History", "Questions about world history, events, and historical figures", "#FF9800", "fas fa-landmark");
            createDefaultCategory("Geography", "Questions about countries, capitals, maps, and world knowledge", "#2196F3", "fas fa-globe");
            createDefaultCategory("Literature", "Questions about books, authors, poetry, and literary works", "#9C27B0", "fas fa-book");
            createDefaultCategory("Technology", "Questions about computers, programming, gadgets, and tech trends", "#607D8B", "fas fa-laptop");
            createDefaultCategory("Sports", "Questions about sports, athletes, games, and competitions", "#F44336", "fas fa-futbol");
            createDefaultCategory("Entertainment", "Questions about movies, music, celebrities, and pop culture", "#E91E63", "fas fa-film");
            createDefaultCategory("General Knowledge", "Mixed questions covering various topics and trivia", "#795548", "fas fa-lightbulb");
        }
    }
}
