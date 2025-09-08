package com.qwizz.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "achievements")
public class Achievement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    @NotBlank(message = "Achievement name is required")
    @Size(max = 100, message = "Achievement name must not exceed 100 characters")
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 50)
    private String icon;
    
    @Column(length = 50)
    private String color;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementType type;
    
    @Column(name = "requirement_value")
    private Integer requirementValue;
    
    @Column(name = "points_reward", columnDefinition = "INT DEFAULT 0")
    private Integer pointsReward = 0;
    
    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "achievement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserAchievement> userAchievements;
    
    public enum AchievementType {
        QUIZ_COMPLETED,
        QUIZ_STREAK,
        HIGH_SCORE,
        QUIZ_CREATED,
        PARTICIPATION,
        POINTS_EARNED,
        PERFECT_SCORE,
        SPEED_DEMON,
        CATEGORY_MASTER,
        SOCIAL_BUTTERFLY
    }
    
    // Constructors
    public Achievement() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Achievement(String name, String description, AchievementType type) {
        this();
        this.name = name;
        this.description = description;
        this.type = type;
    }
    
    public Achievement(String name, String description, AchievementType type, Integer requirementValue, Integer pointsReward) {
        this(name, description, type);
        this.requirementValue = requirementValue;
        this.pointsReward = pointsReward;
    }
    
    // Lifecycle callbacks
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public AchievementType getType() {
        return type;
    }
    
    public void setType(AchievementType type) {
        this.type = type;
    }
    
    public Integer getRequirementValue() {
        return requirementValue;
    }
    
    public void setRequirementValue(Integer requirementValue) {
        this.requirementValue = requirementValue;
    }
    
    public Integer getPointsReward() {
        return pointsReward;
    }
    
    public void setPointsReward(Integer pointsReward) {
        this.pointsReward = pointsReward;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<UserAchievement> getUserAchievements() {
        return userAchievements;
    }
    
    public void setUserAchievements(List<UserAchievement> userAchievements) {
        this.userAchievements = userAchievements;
    }
    
    @Override
    public String toString() {
        return "Achievement{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", requirementValue=" + requirementValue +
                ", pointsReward=" + pointsReward +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
