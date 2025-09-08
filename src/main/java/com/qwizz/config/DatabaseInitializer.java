package com.qwizz.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            // Drop and recreate users table to fix any schema issues
            jdbcTemplate.execute("DROP TABLE IF EXISTS quiz_attempts");
            jdbcTemplate.execute("DROP TABLE IF EXISTS questions");
            jdbcTemplate.execute("DROP TABLE IF EXISTS quizzes");
            jdbcTemplate.execute("DROP TABLE IF EXISTS users");

            // Create users table
            jdbcTemplate.execute("""
                        CREATE TABLE users (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            username VARCHAR(50) UNIQUE NOT NULL,
                            email VARCHAR(100) UNIQUE NOT NULL,
                            password VARCHAR(255) NOT NULL,
                            first_name VARCHAR(50) NOT NULL,
                            last_name VARCHAR(50) NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            active BOOLEAN DEFAULT TRUE,
                            INDEX idx_username (username),
                            INDEX idx_email (email),
                            INDEX idx_active (active)
                        )
                    """);

            // Create quizzes table
            jdbcTemplate.execute("""
                        CREATE TABLE quizzes (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            title VARCHAR(200) NOT NULL,
                            description TEXT,
                            creator_id BIGINT NOT NULL,
                            difficulty ENUM('EASY', 'MEDIUM', 'HARD') DEFAULT 'MEDIUM',
                            time_limit INT DEFAULT 30,
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
                        )
                    """);

            // Create questions table
            jdbcTemplate.execute(
                    """
                                CREATE TABLE questions (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    quiz_id BIGINT NOT NULL,
                                    question_text TEXT NOT NULL,
                                    question_type ENUM('MULTIPLE_CHOICE', 'TRUE_FALSE', 'SHORT_ANSWER') DEFAULT 'MULTIPLE_CHOICE',
                                    correct_answer VARCHAR(500) NOT NULL,
                                    options TEXT,
                                    points INT DEFAULT 10,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    active BOOLEAN DEFAULT TRUE,
                                    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
                                    INDEX idx_quiz_id (quiz_id),
                                    INDEX idx_question_type (question_type),
                                    INDEX idx_active (active)
                                )
                            """);

            // Create quiz_attempts table
            jdbcTemplate.execute("""
                        CREATE TABLE quiz_attempts (
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
                        )
                    """);

            System.out.println("Database tables created successfully!");

            // Insert sample data only if tables are empty
            insertSampleData();

        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertSampleData() {
        try {
            // Check if users table has data
            Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);

            if (userCount == null || userCount == 0) {
                // Insert sample users (password is 'password123' encrypted with BCrypt)
                // BCrypt hash for "password123"
                String hashedPassword = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.";

                jdbcTemplate.update(
                        "INSERT INTO users (username, email, password, first_name, last_name) VALUES (?, ?, ?, ?, ?)",
                        "admin", "admin@qwizz.com", hashedPassword, "Admin", "User");

                jdbcTemplate.update(
                        "INSERT INTO users (username, email, password, first_name, last_name) VALUES (?, ?, ?, ?, ?)",
                        "john_doe", "john@qwizz.com", hashedPassword, "John", "Doe");

                jdbcTemplate.update(
                        "INSERT INTO users (username, email, password, first_name, last_name) VALUES (?, ?, ?, ?, ?)",
                        "jane_smith", "jane@qwizz.com", hashedPassword, "Jane", "Smith");

                // Insert sample quizzes
                jdbcTemplate.update(
                        "INSERT INTO quizzes (title, description, creator_id, difficulty, time_limit, is_public) VALUES (?, ?, ?, ?, ?, ?)",
                        "General Knowledge Quiz", "Test your general knowledge with this fun quiz!", 1, "EASY", 15,
                        true);

                jdbcTemplate.update(
                        "INSERT INTO quizzes (title, description, creator_id, difficulty, time_limit, is_public) VALUES (?, ?, ?, ?, ?, ?)",
                        "Science Quiz", "Challenge yourself with these science questions", 2, "MEDIUM", 20, true);

                jdbcTemplate.update(
                        "INSERT INTO quizzes (title, description, creator_id, difficulty, time_limit, is_public) VALUES (?, ?, ?, ?, ?, ?)",
                        "History Quiz", "How well do you know world history?", 1, "HARD", 25, true);

                // Insert sample questions for General Knowledge Quiz (id: 1)
                jdbcTemplate.update(
                        "INSERT INTO questions (quiz_id, question_text, question_type, correct_answer, options, points) VALUES (?, ?, ?, ?, ?, ?)",
                        1, "What is the capital of France?", "MULTIPLE_CHOICE", "Paris", "Paris|London|Berlin|Madrid",
                        10);

                jdbcTemplate.update(
                        "INSERT INTO questions (quiz_id, question_text, question_type, correct_answer, options, points) VALUES (?, ?, ?, ?, ?, ?)",
                        1, "The Great Wall of China is visible from space.", "TRUE_FALSE", "False", null, 10);

                jdbcTemplate.update(
                        "INSERT INTO questions (quiz_id, question_text, question_type, correct_answer, options, points) VALUES (?, ?, ?, ?, ?, ?)",
                        1, "Which planet is known as the Red Planet?", "MULTIPLE_CHOICE", "Mars",
                        "Mars|Venus|Jupiter|Saturn", 10);

                // Insert sample questions for Science Quiz (id: 2)
                jdbcTemplate.update(
                        "INSERT INTO questions (quiz_id, question_text, question_type, correct_answer, options, points) VALUES (?, ?, ?, ?, ?, ?)",
                        2, "What is the chemical symbol for gold?", "MULTIPLE_CHOICE", "Au", "Au|Ag|Cu|Fe", 15);

                jdbcTemplate.update(
                        "INSERT INTO questions (quiz_id, question_text, question_type, correct_answer, options, points) VALUES (?, ?, ?, ?, ?, ?)",
                        2, "Water boils at 100°C at sea level.", "TRUE_FALSE", "True", null, 10);

                jdbcTemplate.update(
                        "INSERT INTO questions (quiz_id, question_text, question_type, correct_answer, options, points) VALUES (?, ?, ?, ?, ?, ?)",
                        2, "How many chambers does a human heart have?", "MULTIPLE_CHOICE", "4", "2|3|4|5", 15);

                // Insert sample questions for History Quiz (id: 3)
                jdbcTemplate.update(
                        "INSERT INTO questions (quiz_id, question_text, question_type, correct_answer, options, points) VALUES (?, ?, ?, ?, ?, ?)",
                        3, "In which year did World War II end?", "MULTIPLE_CHOICE", "1945", "1943|1944|1945|1946", 20);

                jdbcTemplate.update(
                        "INSERT INTO questions (quiz_id, question_text, question_type, correct_answer, options, points) VALUES (?, ?, ?, ?, ?, ?)",
                        3, "The Roman Empire fell in 476 AD.", "TRUE_FALSE", "True", null, 15);

                jdbcTemplate.update(
                        "INSERT INTO questions (quiz_id, question_text, question_type, correct_answer, options, points) VALUES (?, ?, ?, ?, ?, ?)",
                        3, "Who was the first person to walk on the moon?", "MULTIPLE_CHOICE", "Neil Armstrong",
                        "Neil Armstrong|Buzz Aldrin|John Glenn|Alan Shepard", 20);

                System.out.println("Sample data inserted successfully!");
            } else {
                System.out.println("Sample data already exists, skipping insertion.");
            }

        } catch (Exception e) {
            System.err.println("Error inserting sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
