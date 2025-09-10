-- =============================================
-- QWIZZ DATABASE SCHEMA
-- Modern Quiz Application with Full Features
-- =============================================

DROP DATABASE IF EXISTS qwizz_db;
CREATE DATABASE qwizz_db;
USE qwizz_db;

-- =============================================
-- 1. USERS TABLE - Enhanced with profile features
-- =============================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50),
    role ENUM('TEACHER', 'STUDENT') DEFAULT 'STUDENT',
    profile_picture VARCHAR(255) DEFAULT '/images/default-avatar.png',
    bio TEXT,
    date_of_birth DATE,
    phone VARCHAR(20),
    location VARCHAR(100),
    total_points INT DEFAULT 0,
    quiz_streak INT DEFAULT 0,
    total_quizzes_taken INT DEFAULT 0,
    total_quizzes_created INT DEFAULT 0,
    average_score DECIMAL(5,2) DEFAULT 0.00,
    email_verified BOOLEAN DEFAULT FALSE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_active (active),
    INDEX idx_total_points (total_points)
);

-- =============================================
-- 2. CATEGORIES TABLE - Quiz organization
-- =============================================
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon VARCHAR(50) DEFAULT 'fa-book',
    color VARCHAR(7) DEFAULT '#667eea',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    
    INDEX idx_name (name),
    INDEX idx_active (active)
);

-- =============================================
-- 3. QUIZZES TABLE - Enhanced with categories
-- =============================================
CREATE TABLE quizzes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    creator_id BIGINT NOT NULL,
    category_id BIGINT,
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') DEFAULT 'MEDIUM',
    time_limit INT DEFAULT 30, -- in minutes
    max_attempts INT DEFAULT 3,
    passing_score DECIMAL(5,2) DEFAULT 60.00,
    is_public BOOLEAN DEFAULT TRUE,
    ai_generated BOOLEAN DEFAULT FALSE,
    featured BOOLEAN DEFAULT FALSE,
    total_attempts INT DEFAULT 0,
    average_score DECIMAL(5,2) DEFAULT 0.00,
    show_correct_answers BOOLEAN DEFAULT TRUE,
    randomize_questions BOOLEAN DEFAULT FALSE,
    instant_feedback BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    
    INDEX idx_creator_id (creator_id),
    INDEX idx_category_id (category_id),
    INDEX idx_difficulty (difficulty),
    INDEX idx_is_public (is_public),
    INDEX idx_featured (featured),
    INDEX idx_active (active),
    INDEX idx_created_at (created_at)
);

-- =============================================
-- 4. QUESTIONS TABLE - Enhanced question types
-- =============================================
CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    question_number INT NOT NULL,
    question_text TEXT NOT NULL,
    question_type ENUM('MULTIPLE_CHOICE', 'TRUE_FALSE', 'SHORT_ANSWER', 'ESSAY') DEFAULT 'MULTIPLE_CHOICE',
    correct_answer TEXT NOT NULL,
    options TEXT, -- JSON format for multiple choice
    explanation TEXT,
    points DECIMAL(5,2) DEFAULT 10.00,
    time_limit INT, -- optional per-question time limit
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    
    INDEX idx_quiz_id (quiz_id),
    INDEX idx_question_number (question_number),
    INDEX idx_question_type (question_type),
    INDEX idx_active (active)
);

-- =============================================
-- 5. QUIZ ATTEMPTS TABLE - Comprehensive tracking
-- =============================================
CREATE TABLE quiz_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    quiz_id BIGINT NOT NULL,
    attempt_number INT DEFAULT 1,
    score DECIMAL(5,2) DEFAULT 0.00,
    percentage DECIMAL(5,2) DEFAULT 0.00,
    total_questions INT NOT NULL,
    correct_answers INT DEFAULT 0,
    wrong_answers INT DEFAULT 0,
    skipped_answers INT DEFAULT 0,
    total_points DECIMAL(5,2) DEFAULT 0.00,
    time_taken INT, -- in seconds
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    completed BOOLEAN DEFAULT FALSE,
    passed BOOLEAN DEFAULT FALSE,
    status ENUM('STARTED', 'IN_PROGRESS', 'COMPLETED', 'ABANDONED', 'EXPIRED') DEFAULT 'STARTED',
    ip_address VARCHAR(45),
    user_agent TEXT,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_quiz_id (quiz_id),
    INDEX idx_completed (completed),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_percentage (percentage)
);

-- =============================================
-- 6. ANSWERS TABLE - Individual question answers
-- =============================================
CREATE TABLE answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    user_answer TEXT,
    is_correct BOOLEAN DEFAULT FALSE,
    points_earned DECIMAL(5,2) DEFAULT 0.00,
    time_taken INT, -- in seconds
    answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    
    INDEX idx_attempt_id (attempt_id),
    INDEX idx_question_id (question_id),
    INDEX idx_is_correct (is_correct)
);

-- =============================================
-- 7. ACHIEVEMENTS TABLE - Gamification system
-- =============================================
CREATE TABLE achievements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon VARCHAR(50) DEFAULT 'fa-trophy',
    color VARCHAR(50) DEFAULT '#ffd700',
    type ENUM('QUIZ_COMPLETED', 'QUIZ_STREAK', 'HIGH_SCORE', 'QUIZ_CREATED', 'PARTICIPATION', 'POINTS_EARNED', 'PERFECT_SCORE', 'SPEED_DEMON', 'CATEGORY_MASTER', 'SOCIAL_BUTTERFLY') NOT NULL,
    requirement_value INT,
    points_reward INT DEFAULT 100,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_type (type),
    INDEX idx_is_active (is_active)
);

-- =============================================
-- 8. USER ACHIEVEMENTS TABLE - Earned badges
-- =============================================
CREATE TABLE user_achievements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    achievement_id BIGINT NOT NULL,
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    progress_value INT,
    notes TEXT,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (achievement_id) REFERENCES achievements(id) ON DELETE CASCADE,
    
    UNIQUE KEY unique_user_achievement (user_id, achievement_id),
    INDEX idx_user_id (user_id),
    INDEX idx_achievement_id (achievement_id),
    INDEX idx_earned_at (earned_at)
);

-- =============================================
-- 9. LEADERBOARDS TABLE - Competition tracking
-- =============================================
CREATE TABLE leaderboards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    quiz_id BIGINT,
    category_id BIGINT,
    leaderboard_type ENUM('GLOBAL', 'QUIZ', 'CATEGORY', 'WEEKLY', 'MONTHLY') NOT NULL,
    total_points DECIMAL(10,2) DEFAULT 0.00,
    total_quizzes INT DEFAULT 0,
    average_score DECIMAL(5,2) DEFAULT 0.00,
    best_score DECIMAL(5,2) DEFAULT 0.00,
    rank_position INT DEFAULT 0,
    week_year VARCHAR(7), -- format: 2025-01
    month_year VARCHAR(7), -- format: 2025-01
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_quiz_id (quiz_id),
    INDEX idx_category_id (category_id),
    INDEX idx_leaderboard_type (leaderboard_type),
    INDEX idx_rank_position (rank_position),
    INDEX idx_total_points (total_points)
);

-- =============================================
-- SEED DATA INSERTION
-- =============================================

-- Insert Categories
INSERT INTO categories (name, description, icon, color) VALUES
('Programming', 'Software development and coding quizzes', 'fa-code', '#667eea'),
('Mathematics', 'Mathematical concepts and problem solving', 'fa-calculator', '#764ba2'),
('Science', 'Physics, Chemistry, Biology quizzes', 'fa-flask', '#FF6B35'),
('Technology', 'IT, Computer Science, and Tech trends', 'fa-laptop', '#00D2FF'),
('Engineering', 'Electrical, Mechanical, Civil Engineering', 'fa-cogs', '#FF3366'),
('General Knowledge', 'Mixed topics and trivia', 'fa-brain', '#9D4EDD');

-- Insert Sample Users (password: 'password123' - BCrypt encoded)
INSERT INTO users (username, email, password, first_name, last_name, role, bio, total_points) VALUES
('john_doe', 'john.doe@qwizz.com', '$2a$10$eUz1YrEeZgqQ8Qx8VrIkVeY.Lj3rQ4X5nKmL8pQrS6tUvWxYz0A1B2', 'John', 'Doe', 'TEACHER', 'Senior Programming Instructor with 10+ years experience', 3200),
('jane_smith', 'jane.smith@qwizz.com', '$2a$10$eUz1YrEeZgqQ8Qx8VrIkVeY.Lj3rQ4X5nKmL8pQrS6tUvWxYz0A1B2', 'Jane', 'Smith', 'STUDENT', 'Computer Science student passionate about learning', 1850),
('mike_wilson', 'mike.wilson@qwizz.com', '$2a$10$eUz1YrEeZgqQ8Qx8VrIkVeY.Lj3rQ4X5nKmL8pQrS6tUvWxYz0A1B2', 'Mike', 'Wilson', 'TEACHER', 'Mathematics and Physics educator', 2900),
('sarah_lee', 'sarah.lee@qwizz.com', '$2a$10$eUz1YrEeZgqQ8Qx8VrIkVeY.Lj3rQ4X5nKmL8pQrS6tUvWxYz0A1B2', 'Sarah', 'Lee', 'STUDENT', 'Engineering student with focus on programming', 2100),
('alex_brown', 'alex.brown@qwizz.com', '$2a$10$eUz1YrEeZgqQ8Qx8VrIkVeY.Lj3rQ4X5nKmL8pQrS6tUvWxYz0A1B2', 'Alex', 'Brown', 'STUDENT', 'Mathematics enthusiast and problem solver', 1650);

-- Insert Sample Quizzes (using creator_id 1 and 3 which are teachers)
INSERT INTO quizzes (title, description, creator_id, category_id, difficulty, time_limit, max_attempts, passing_score, featured, total_attempts, average_score, show_correct_answers, randomize_questions, instant_feedback) VALUES
('Java Fundamentals', 'Master the basics of Java programming including OOP concepts', 1, 1, 'MEDIUM', 30, 3, 70.00, TRUE, 45, 78.50, TRUE, FALSE, TRUE),
('Advanced SQL Queries', 'Complex database operations, joins, and optimization techniques', 1, 4, 'HARD', 45, 2, 75.00, TRUE, 32, 82.30, TRUE, FALSE, TRUE),
('Linear Algebra Basics', 'Vectors, matrices, and fundamental linear algebra operations', 3, 2, 'MEDIUM', 40, 3, 65.00, FALSE, 28, 75.20, TRUE, FALSE, TRUE),
('Web Development Essentials', 'HTML, CSS, JavaScript fundamentals for modern web development', 1, 1, 'EASY', 25, 5, 60.00, TRUE, 67, 85.10, TRUE, TRUE, TRUE),
('Physics Mechanics', 'Classical mechanics, motion, forces, and energy concepts', 3, 3, 'HARD', 50, 2, 70.00, FALSE, 19, 68.90, TRUE, FALSE, TRUE),
('Data Structures & Algorithms', 'Arrays, linked lists, trees, sorting, and searching algorithms', 1, 1, 'HARD', 60, 2, 80.00, TRUE, 41, 71.40, TRUE, FALSE, TRUE);

-- Insert Sample Questions for Java Fundamentals Quiz
INSERT INTO questions (quiz_id, question_number, question_text, question_type, correct_answer, options, explanation, points) VALUES
(1, 1, 'Which keyword is used to inherit a class in Java?', 'MULTIPLE_CHOICE', 'extends', '["extends", "implements", "super", "inherit"]', 'The extends keyword is used for class inheritance in Java', 10.00),
(1, 2, 'Java is platform independent because of JVM.', 'TRUE_FALSE', 'true', '["true", "false"]', 'JVM (Java Virtual Machine) makes Java platform independent by providing an abstraction layer', 10.00),
(1, 3, 'Which of these is NOT a Java primitive type?', 'MULTIPLE_CHOICE', 'String', '["int", "boolean", "char", "String"]', 'String is a class in Java, not a primitive type', 15.00),
(1, 4, 'What will be the output of: System.out.println(10/3);', 'MULTIPLE_CHOICE', '3', '["3", "3.33", "3.0", "Error"]', 'Integer division in Java truncates the decimal part', 15.00),
(1, 5, 'Which access modifier provides the most restrictive access?', 'MULTIPLE_CHOICE', 'private', '["public", "protected", "private", "default"]', 'Private access modifier restricts access to the same class only', 10.00);

-- Insert Sample Questions for SQL Quiz
INSERT INTO questions (quiz_id, question_number, question_text, question_type, correct_answer, options, explanation, points) VALUES
(2, 1, 'Which SQL command is used to remove a table from the database?', 'MULTIPLE_CHOICE', 'DROP', '["DELETE", "TRUNCATE", "DROP", "REMOVE"]', 'DROP TABLE removes the entire table structure and data', 15.00),
(2, 2, 'PRIMARY KEY can have NULL values.', 'TRUE_FALSE', 'false', '["true", "false"]', 'Primary keys must be unique and cannot contain NULL values', 10.00),
(2, 3, 'Which SQL clause is used to filter rows?', 'MULTIPLE_CHOICE', 'WHERE', '["WHERE", "HAVING", "GROUP BY", "ORDER BY"]', 'WHERE clause filters rows based on specified conditions', 15.00),
(2, 4, 'What does the SQL JOIN clause do?', 'MULTIPLE_CHOICE', 'Combine rows from two or more tables', '["Delete rows", "Combine rows from two or more tables", "Sort data", "Update records"]', 'JOIN combines rows from related tables based on a common column', 20.00);

-- Insert Sample Achievements
INSERT INTO achievements (name, description, icon, color, type, requirement_value, points_reward) VALUES
('First Steps', 'Complete your first quiz', 'fa-baby', '#4CAF50', 'QUIZ_COMPLETED', 1, 50),
('Quiz Master', 'Complete 10 quizzes', 'fa-crown', '#FFD700', 'QUIZ_COMPLETED', 10, 200),
('Perfect Score', 'Achieve 100% on any quiz', 'fa-star', '#FF6B35', 'PERFECT_SCORE', 100, 150),
('Speed Demon', 'Complete a quiz in under 5 minutes', 'fa-rocket', '#00D2FF', 'SPEED_DEMON', 300, 100),
('Week Warrior', 'Maintain a 7-day quiz streak', 'fa-fire', '#FF3366', 'QUIZ_STREAK', 7, 300);

-- Insert Sample Quiz Attempts (using correct user IDs 2, 4, 5 which are students)
INSERT INTO quiz_attempts (user_id, quiz_id, attempt_number, score, percentage, total_questions, correct_answers, wrong_answers, total_points, time_taken, completed, passed, status, end_time) VALUES
(2, 1, 1, 85.00, 85.00, 5, 4, 1, 50.00, 1200, TRUE, TRUE, 'COMPLETED', DATE_ADD(NOW(), INTERVAL -2 DAY)),
(2, 2, 1, 75.00, 75.00, 4, 3, 1, 60.00, 1800, TRUE, TRUE, 'COMPLETED', DATE_ADD(NOW(), INTERVAL -1 DAY)),
(4, 1, 1, 90.00, 90.00, 5, 5, 0, 60.00, 1100, TRUE, TRUE, 'COMPLETED', DATE_ADD(NOW(), INTERVAL -3 DAY)),
(4, 4, 1, 95.00, 95.00, 4, 4, 0, 40.00, 900, TRUE, TRUE, 'COMPLETED', DATE_ADD(NOW(), INTERVAL -1 DAY)),
(5, 3, 1, 70.00, 70.00, 5, 3, 2, 45.00, 1500, TRUE, TRUE, 'COMPLETED', DATE_ADD(NOW(), INTERVAL -4 DAY)),
(5, 1, 1, 60.00, 60.00, 5, 3, 2, 45.00, 1600, TRUE, FALSE, 'COMPLETED', DATE_ADD(NOW(), INTERVAL -2 DAY));

-- Insert Sample User Achievements (using correct user IDs)
INSERT INTO user_achievements (user_id, achievement_id, earned_at) VALUES
(2, 1, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(2, 3, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(4, 1, DATE_ADD(NOW(), INTERVAL -3 DAY)),
(4, 3, DATE_ADD(NOW(), INTERVAL -1 DAY)),
(5, 1, DATE_ADD(NOW(), INTERVAL -4 DAY));

-- Update user total points based on quiz attempts
UPDATE users SET total_points = (
    SELECT COALESCE(SUM(total_points), 0) 
    FROM quiz_attempts 
    WHERE quiz_attempts.user_id = users.id AND completed = TRUE
) WHERE role = 'STUDENT';

-- =============================================
-- END OF SCHEMA
-- =============================================
