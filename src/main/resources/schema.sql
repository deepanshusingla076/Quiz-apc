
CREATE DATABASE IF NOT EXISTS qwizz_db;
USE qwizz_db;

-- Users table with role support
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role ENUM('TEACHER', 'STUDENT', 'ADMIN') DEFAULT 'STUDENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_active (active)
);

-- Quizzes table with enhanced fields
CREATE TABLE IF NOT EXISTS quizzes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    creator_id BIGINT NOT NULL,
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') DEFAULT 'MEDIUM',
    time_limit INT DEFAULT 30, -- in minutes
    is_public BOOLEAN DEFAULT TRUE,
    ai_generated BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (creator_id) REFERENCES users(id),
    INDEX idx_creator_id (creator_id),
    INDEX idx_difficulty (difficulty),
    INDEX idx_is_public (is_public),
    INDEX idx_active (active),
    INDEX idx_created_at (created_at)
);

-- Questions table with enhanced question types
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    question_type ENUM('MULTIPLE_CHOICE', 'TRUE_FALSE', 'SHORT_ANSWER') DEFAULT 'MULTIPLE_CHOICE',
    correct_answer VARCHAR(500) NOT NULL,
    options TEXT, -- Pipe-separated values for multiple choice
    points INT DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    INDEX idx_quiz_id (quiz_id),
    INDEX idx_question_type (question_type),
    INDEX idx_active (active)
);

-- Quiz attempts table with enhanced tracking
CREATE TABLE IF NOT EXISTS quiz_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    quiz_id BIGINT NOT NULL,
    score INT DEFAULT 0,
    total_questions INT NOT NULL,
    correct_answers INT DEFAULT 0,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    completed BOOLEAN DEFAULT FALSE,
    status ENUM('STARTED', 'COMPLETED', 'ABANDONED') DEFAULT 'STARTED',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id),
    INDEX idx_user_id (user_id),
    INDEX idx_quiz_id (quiz_id),
    INDEX idx_completed (completed),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
);

-- Insert sample data with roles
-- Sample users with different roles (password is 'password123' encrypted with BCrypt)
INSERT IGNORE INTO users (username, email, password, first_name, last_name, role) VALUES
('admin', 'admin@qwizz.com', '$2a$10$8GXO8K8lhL4K8uGOx1K6qOJc.3fQ3Kp5h9F8K8L8qOJc.3fQ3Kp5h9', 'Admin', 'User', 'ADMIN'),
('teacher1', 'teacher1@qwizz.com', '$2a$10$8GXO8K8lhL4K8uGOx1K6qOJc.3fQ3Kp5h9F8K8L8qOJc.3fQ3Kp5h9', 'John', 'Teacher', 'TEACHER'),
('teacher2', 'teacher2@qwizz.com', '$2a$10$8GXO8K8lhL4K8uGOx1K6qOJc.3fQ3Kp5h9F8K8L8qOJc.3fQ3Kp5h9', 'Jane', 'Professor', 'TEACHER'),
('student1', 'student1@qwizz.com', '$2a$10$8GXO8K8lhL4K8uGOx1K6qOJc.3fQ3Kp5h9F8K8L8qOJc.3fQ3Kp5h9', 'Alice', 'Student', 'STUDENT'),
('student2', 'student2@qwizz.com', '$2a$10$8GXO8K8lhL4K8uGOx1K6qOJc.3fQ3Kp5h9F8K8L8qOJc.3fQ3Kp5h9', 'Bob', 'Learner', 'STUDENT'),
('student3', 'student3@qwizz.com', '$2a$10$8GXO8K8lhL4K8uGOx1K6qOJc.3fQ3Kp5h9F8K8L8qOJc.3fQ3Kp5h9', 'Charlie', 'Smith', 'STUDENT');

-- Sample quizzes created by teachers
INSERT IGNORE INTO quizzes (title, description, creator_id, difficulty, time_limit, is_public) VALUES
('Java Basics Quiz', 'Test your knowledge of Java programming fundamentals', 3, 'MEDIUM', 25, TRUE),
('SQL Mastery Quiz', 'Queries, keys, normalization, and advanced SQL concepts', 2, 'HARD', 30, TRUE),
('BEE Fundamentals Quiz', 'Basic Electrical Engineering concepts and circuits', 4, 'MEDIUM', 20, TRUE),
('DSA Concepts Quiz', 'Core data structures and algorithms questions', 3, 'HARD', 40, TRUE);

-- Sample questions for General Knowledge Quiz (id: 1)
INSERT IGNORE INTO questions (quiz_id, question_text, question_type, correct_answer, options, points) VALUES
(1, 'Which keyword is used to inherit a class in Java?', 'MULTIPLE_CHOICE', 'extends', 'extends|implements|super|import', 10),
(1, 'Java is platform independent because of JVM.', 'TRUE_FALSE', 'True', NULL, 10),
(1, 'Which of these is not a Java primitive type?', 'MULTIPLE_CHOICE', 'String', 'int|float|boolean|String', 15),
(1, 'What will be the output of: System.out.println(10/3);', 'MULTIPLE_CHOICE', '3', '3|3.33|3.0|Error', 15);


-- Sample questions for Advanced Science Quiz (id: 2)
INSERT IGNORE INTO questions (quiz_id, question_text, question_type, correct_answer, options, points) VALUES
(2, 'Which SQL command is used to remove a table from the database?', 'MULTIPLE_CHOICE', 'DROP', 'DELETE|TRUNCATE|DROP|REMOVE', 15),
(2, 'PRIMARY KEY can have NULL values.', 'TRUE_FALSE', 'False', NULL, 10),
(2, 'Which SQL clause is used to filter rows?', 'MULTIPLE_CHOICE', 'WHERE', 'WHERE|HAVING|GROUP BY|ORDER BY', 15),
(2, 'What does the SQL JOIN clause do?', 'MULTIPLE_CHOICE', 'Combine rows from two or more tables', 'Delete rows|Combine rows from two or more tables|Sort data|Update records', 20);

-- Sample questions for Programming Fundamentals (id: 3)
INSERT IGNORE INTO questions (quiz_id, question_text, question_type, correct_answer, options, points) VALUES
(3, 'What is the SI unit of electric current?', 'MULTIPLE_CHOICE', 'Ampere', 'Ampere|Volt|Ohm|Coulomb', 10),
(3, 'Ohm’s Law states V = IR.', 'TRUE_FALSE', 'True', NULL, 10),
(3, 'Which of these is an AC source?', 'MULTIPLE_CHOICE', 'Alternator', 'Battery|Alternator|Solar Cell|Fuel Cell', 15),
(3, 'Power factor of a purely resistive circuit is?', 'MULTIPLE_CHOICE', '1', '0|0.5|1|Depends on frequency', 15);

-- Sample questions for History of Technology (id: 4)
INSERT IGNORE INTO questions (quiz_id, question_text, question_type, correct_answer, options, points) VALUES
(4, 'Which data structure works on FIFO principle?', 'MULTIPLE_CHOICE', 'Queue', 'Stack|Queue|Tree|Graph', 10),
(4, 'A binary search tree (BST) is always balanced.', 'TRUE_FALSE', 'False', NULL, 10),
(4, 'Time complexity of binary search in a sorted array?', 'MULTIPLE_CHOICE', 'O(log n)', 'O(n)|O(log n)|O(n log n)|O(1)', 15),
(4, 'Which sorting algorithm has best average case time complexity?', 'MULTIPLE_CHOICE', 'Merge Sort', 'Bubble Sort|Selection Sort|Merge Sort|Insertion Sort', 20);

-- Sample questions for Mathematics Challenge (id: 5)
INSERT IGNORE INTO questions (quiz_id, question_text, question_type, correct_answer, options, points) VALUES
(5, 'Which scheduling algorithm is also called First Come First Serve?', 'MULTIPLE_CHOICE', 'FCFS', 'FCFS|SJF|Round Robin|Priority', 10),
(5, 'Deadlock can occur if mutual exclusion holds.', 'TRUE_FALSE', 'True', NULL, 10),
(5, 'Which memory management technique suffers from external fragmentation?', 'MULTIPLE_CHOICE', 'Contiguous allocation', 'Paging|Segmentation|Contiguous allocation|Virtual memory', 15),
(5, 'Which of the following is NOT a valid page replacement algorithm?', 'MULTIPLE_CHOICE', 'DFS', 'FIFO|LRU|Optimal|DFS', 15),
(5, 'Which register keeps track of the address of the next instruction?', 'MULTIPLE_CHOICE', 'Program Counter', 'Program Counter|Stack Pointer|Instruction Register|MAR', 20);

-- Sample quiz attempts
INSERT IGNORE INTO quiz_attempts (user_id, quiz_id, score, total_questions, correct_answers, start_time, end_time, completed, status) VALUES
(4, 1, 40, 5, 4, '2024-01-15 10:00:00', '2024-01-15 10:12:00', TRUE, 'COMPLETED'),
(4, 2, 50, 5, 4, '2024-01-16 14:30:00', '2024-01-16 14:55:00', TRUE, 'COMPLETED'),
(5, 3, 35, 5, 3, '2024-01-17 09:15:00', '2024-01-17 09:25:00', TRUE, 'COMPLETED'),
(5, 4, 45, 5, 4, '2024-01-18 11:00:00', '2024-01-18 11:20:00', TRUE, 'COMPLETED'),
(6, 1, 25, 5, 2, '2024-01-19 15:45:00', '2024-01-19 15:58:00', TRUE, 'COMPLETED'),
(6, 5, 30, 5, 3, '2024-01-20 13:30:00', '2024-01-20 13:45:00', TRUE, 'COMPLETED');