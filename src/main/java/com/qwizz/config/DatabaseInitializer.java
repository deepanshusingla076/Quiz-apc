package com.qwizz.config;

import com.qwizz.model.*;
import com.qwizz.service.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
@Transactional
public class DatabaseInitializer {

    private final UserService userService;
    private final CategoryService categoryService;
    private final AchievementService achievementService;
    private final QuizService quizService;
    private final QuizAttemptService quizAttemptService;

    private final Random random = new Random();

    public DatabaseInitializer(UserService userService,
                              CategoryService categoryService,
                              AchievementService achievementService,
                              QuizService quizService,
                              QuizAttemptService quizAttemptService) {
        this.userService = userService;
        this.categoryService = categoryService;
        this.achievementService = achievementService;
        this.quizService = quizService;
        this.quizAttemptService = quizAttemptService;
    }

    @PostConstruct
    public void initializeDatabase() {
        try {
            // Initialize in order of dependencies
            initializeCategories();
            initializeAchievements();
            initializeUsers();
            initializeQuizzes();
            initializeQuizAttempts();
            initializeUserAchievements();
            
            System.out.println("🎉 Database initialized successfully with comprehensive sample data!");
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeCategories() {
        categoryService.initializeDefaultCategories();
        System.out.println("✅ Categories initialized");
    }

    private void initializeAchievements() {
        achievementService.initializeDefaultAchievements();
        System.out.println("✅ Achievements initialized");
    }

    private void initializeUsers() {
        // Create demo users if none exist
        if (userService.getAllUsers().isEmpty()) {
            // Create admin user
            User admin = new User("admin", "admin@qwizz.com", "password123", "Admin", "User", Role.TEACHER);
            admin.setTotalPoints(5000);
            admin.setQuizStreak(15);
            admin.setTotalQuizzesTaken(100);
            admin.setTotalQuizzesCreated(25);
            admin.setAverageScore(92.5);
            admin.setBio("Platform administrator and quiz creator extraordinaire");
            admin.setLocation("San Francisco, CA");
            admin.setEmailVerified(true);
            userService.registerUser(admin);

            // Create sample teacher
            User teacher = new User("teacher1", "teacher@qwizz.com", "password123", "Sarah", "Johnson", Role.TEACHER);
            teacher.setTotalPoints(3500);
            teacher.setQuizStreak(8);
            teacher.setTotalQuizzesTaken(75);
            teacher.setTotalQuizzesCreated(15);
            teacher.setAverageScore(88.3);
            teacher.setBio("Passionate educator creating engaging quiz content");
            teacher.setLocation("New York, NY");
            teacher.setEmailVerified(true);
            userService.registerUser(teacher);

            // Create sample students
            String[] firstNames = {"John", "Emily", "Michael", "Jessica", "David", "Ashley", "Christopher", "Amanda", "Matthew", "Jennifer"};
            String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez"};
            String[] cities = {"Los Angeles, CA", "Chicago, IL", "Houston, TX", "Phoenix, AZ", "Philadelphia, PA", "San Antonio, TX", "San Diego, CA", "Dallas, TX", "San Jose, CA", "Austin, TX"};

            for (int i = 0; i < 10; i++) {
                String firstName = firstNames[i];
                String lastName = lastNames[i];
                String username = firstName.toLowerCase() + lastName.toLowerCase() + (i + 1);
                String email = username + "@qwizz.com";

                User student = new User(username, email, "password123", firstName, lastName, Role.STUDENT);
                student.setTotalPoints(random.nextInt(2000) + 100);
                student.setQuizStreak(random.nextInt(20));
                student.setTotalQuizzesTaken(random.nextInt(50) + 5);
                student.setTotalQuizzesCreated(random.nextInt(3));
                student.setAverageScore(60.0 + random.nextDouble() * 35); // 60-95%
                student.setBio("Quiz enthusiast and lifelong learner");
                student.setLocation(cities[random.nextInt(cities.length)]);
                student.setEmailVerified(true);
                userService.registerUser(student);
            }

            System.out.println("✅ Sample users created (12 total: 1 admin, 1 teacher, 10 students)");
        }
    }

    private void initializeQuizzes() {
        List<User> teachers = userService.getUsersByRole(Role.TEACHER);
        List<Category> categories = categoryService.getAllActiveCategories();
        
        if (!teachers.isEmpty() && !categories.isEmpty() && quizService.getAllQuizzes().isEmpty()) {
            
            // Science Quizzes
            createQuizWithQuestions(teachers.get(0), categories.get(0), "Basic Physics", "Test your knowledge of fundamental physics concepts", Difficulty.EASY,
                Arrays.asList(
                    new QuestionData("What is the speed of light in vacuum?", QuestionType.MULTIPLE_CHOICE, 
                        Arrays.asList("299,792,458 m/s", "300,000 km/s", "186,000 mph", "3 × 10^8 m/s"), "299,792,458 m/s", 10),
                    new QuestionData("Which law states that force equals mass times acceleration?", QuestionType.MULTIPLE_CHOICE,
                        Arrays.asList("Newton's First Law", "Newton's Second Law", "Newton's Third Law", "Einstein's Law"), "Newton's Second Law", 10),
                    new QuestionData("What is the basic unit of electric current?", QuestionType.MULTIPLE_CHOICE,
                        Arrays.asList("Volt", "Ampere", "Watt", "Ohm"), "Ampere", 10),
                    new QuestionData("The Earth revolves around the Sun. True or False?", QuestionType.TRUE_FALSE,
                        Arrays.asList("True", "False"), "True", 10)
                ));

            createQuizWithQuestions(teachers.get(0), categories.get(0), "Advanced Chemistry", "Challenge yourself with complex chemistry problems", Difficulty.HARD,
                Arrays.asList(
                    new QuestionData("What is Avogadro's number?", QuestionType.MULTIPLE_CHOICE,
                        Arrays.asList("6.022 × 10^23", "6.626 × 10^-34", "3.14159", "2.998 × 10^8"), "6.022 × 10^23", 15),
                    new QuestionData("Which element has the highest electronegativity?", QuestionType.MULTIPLE_CHOICE,
                        Arrays.asList("Oxygen", "Fluorine", "Chlorine", "Nitrogen"), "Fluorine", 15)
                ));

            // History Quizzes
            createQuizWithQuestions(teachers.get(1), categories.get(1), "World War II", "Test your knowledge of the Second World War", Difficulty.MEDIUM,
                Arrays.asList(
                    new QuestionData("In which year did World War II begin?", QuestionType.MULTIPLE_CHOICE,
                        Arrays.asList("1938", "1939", "1940", "1941"), "1939", 10),
                    new QuestionData("Who was the leader of Nazi Germany?", QuestionType.MULTIPLE_CHOICE,
                        Arrays.asList("Heinrich Himmler", "Hermann Göring", "Adolf Hitler", "Joseph Goebbels"), "Adolf Hitler", 10),
                    new QuestionData("The attack on Pearl Harbor happened on December 7, 1941. True or False?", QuestionType.TRUE_FALSE,
                        Arrays.asList("True", "False"), "True", 10)
                ));

            // Geography Quizzes
            createQuizWithQuestions(teachers.get(0), categories.get(2), "World Capitals", "Can you name the capitals of these countries?", Difficulty.EASY,
                Arrays.asList(
                    new QuestionData("What is the capital of France?", QuestionType.MULTIPLE_CHOICE,
                        Arrays.asList("Lyon", "Marseille", "Paris", "Nice"), "Paris", 10),
                    new QuestionData("What is the capital of Australia?", QuestionType.MULTIPLE_CHOICE,
                        Arrays.asList("Sydney", "Melbourne", "Canberra", "Perth"), "Canberra", 10),
                    new QuestionData("Tokyo is the capital of Japan. True or False?", QuestionType.TRUE_FALSE,
                        Arrays.asList("True", "False"), "True", 10)
                ));

            // Technology Quizzes
            createQuizWithQuestions(teachers.get(1), categories.get(4), "Programming Basics", "Test your programming knowledge", Difficulty.MEDIUM,
                Arrays.asList(
                    new QuestionData("Which of the following is a programming language?", QuestionType.MULTIPLE_CHOICE,
                        Arrays.asList("HTML", "CSS", "Python", "JSON"), "Python", 10),
                    new QuestionData("What does CPU stand for?", QuestionType.MULTIPLE_CHOICE,
                        Arrays.asList("Computer Processing Unit", "Central Processing Unit", "Core Processing Unit", "Central Program Unit"), "Central Processing Unit", 10),
                    new QuestionData("Binary code uses only 0s and 1s. True or False?", QuestionType.TRUE_FALSE,
                        Arrays.asList("True", "False"), "True", 10)
                ));

            // Sports Quizzes
            createQuizWithQuestions(teachers.get(0), categories.get(5), "Football Trivia", "Test your football knowledge", Difficulty.EASY,
                Arrays.asList(
                    new QuestionData("How many players are on a football team on the field at one time?", QuestionType.MULTIPLE_CHOICE,
                        Arrays.asList("10", "11", "12", "13"), "11", 10),
                    new QuestionData("Which country won the 2018 FIFA World Cup?", QuestionType.MULTIPLE_CHOICE,
                        Arrays.asList("Brazil", "Germany", "France", "Argentina"), "France", 10),
                    new QuestionData("A football match consists of two halves. True or False?", QuestionType.TRUE_FALSE,
                        Arrays.asList("True", "False"), "True", 10)
                ));

            // Literature Quizzes
            createQuizWithQuestions(teachers.get(1), categories.get(3), "Classic Literature", "Test your knowledge of classic books", Difficulty.MEDIUM,
                Arrays.asList(
                    new QuestionData("Who wrote 'Romeo and Juliet'?", QuestionType.MULTIPLE_CHOICE,
                        Arrays.asList("Charles Dickens", "William Shakespeare", "Jane Austen", "Mark Twain"), "William Shakespeare", 10),
                    new QuestionData("Which novel begins with 'It was the best of times, it was the worst of times'?", QuestionType.MULTIPLE_CHOICE,
                        Arrays.asList("Pride and Prejudice", "Great Expectations", "A Tale of Two Cities", "Oliver Twist"), "A Tale of Two Cities", 15),
                    new QuestionData("George Orwell wrote '1984'. True or False?", QuestionType.TRUE_FALSE,
                        Arrays.asList("True", "False"), "True", 10)
                ));

            System.out.println("✅ Sample quizzes created with questions (7 quizzes across 6 categories)");
        }
    }

    private void createQuizWithQuestions(User creator, Category category, String title, String description, Difficulty difficulty, List<QuestionData> questionsData) {
        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setDescription(description);
        quiz.setCreatorId(creator.getId());
        quiz.setCategory(category);
        quiz.setDifficulty(difficulty);
        quiz.setTimeLimit(questionsData.size() * 2); // 2 minutes per question
        quiz.setPublic(true);
        quiz.setAiGenerated(false);
        quiz.setTotalAttempts(random.nextInt(50) + 10);
        quiz.setAverageScore(70.0 + random.nextDouble() * 25); // 70-95%
        quiz.setShowCorrectAnswers(true);
        quiz.setInstantFeedback(true);

        Quiz savedQuiz = quizService.createQuiz(quiz);

        // Add questions
        int questionOrder = 1;
        for (QuestionData questionData : questionsData) {
            Question question = new Question();
            question.setQuizId(savedQuiz.getId());
            question.setQuestionText(questionData.questionText);
            question.setQuestionType(questionData.questionType);
            question.setCorrectAnswer(questionData.correctAnswer);
            question.setPoints(questionData.points);
            question.setQuestionOrder(questionOrder++);
            question.setDifficultyLevel(difficulty);
            
            if (!questionData.options.isEmpty()) {
                question.setOptionsList(questionData.options);
            }
            
            quizService.addQuestionToQuiz(savedQuiz.getId(), question);
        }

        // Update creator statistics
        userService.incrementUserQuizzesCreated(creator.getId());
    }

    private void initializeQuizAttempts() {
        List<User> students = userService.getUsersByRole(Role.STUDENT);
        List<Quiz> quizzes = quizService.getPublicQuizzes();

        if (!students.isEmpty() && !quizzes.isEmpty()) {
            // Create quiz attempts for students
            for (User student : students) {
                int attemptsCount = random.nextInt(15) + 5; // 5-20 attempts per student
                
                for (int i = 0; i < attemptsCount; i++) {
                    Quiz randomQuiz = quizzes.get(random.nextInt(quizzes.size()));
                    
                    try {
                        // Create attempt
                        QuizAttempt attempt = quizAttemptService.startQuizAttempt(student.getId(), randomQuiz.getId());
                        
                        // Simulate completion
                        int totalQuestions = attempt.getTotalQuestions();
                        
                        // Skip if quiz has no questions to avoid NaN values
                        if (totalQuestions == 0) {
                            continue;
                        }
                        
                        int correctAnswers = (int) (totalQuestions * (0.4 + random.nextDouble() * 0.6)); // 40-100% correct
                        int score = correctAnswers * 10; // 10 points per correct answer
                        double percentage = (double) correctAnswers / totalQuestions * 100;
                        
                        attempt.setCorrectAnswers(correctAnswers);
                        attempt.setScore(score);
                        attempt.setPercentage(percentage);
                        attempt.setTimeTaken(random.nextInt(1800) + 300); // 5-35 minutes
                        attempt.setTotalPoints((double) totalQuestions * 10);
                        
                        // Set random completion time in the past
                        LocalDateTime startTime = LocalDateTime.now().minusDays(random.nextInt(30));
                        LocalDateTime endTime = startTime.plusMinutes(random.nextInt(30) + 5);
                        attempt.setStartTime(startTime);
                        attempt.setEndTime(endTime);
                        
                        quizAttemptService.submitQuizAttempt(attempt.getId(), correctAnswers, score);
                        
                        // Update quiz statistics
                        quizService.updateQuizStatistics(randomQuiz, percentage);
                    } catch (Exception e) {
                        // Skip this attempt if there's an error
                        System.err.println("Error creating quiz attempt: " + e.getMessage());
                    }
                }
            }
            
            System.out.println("✅ Sample quiz attempts created (50-200 attempts across all students)");
        }
    }

    private void initializeUserAchievements() {
        List<User> users = userService.getAllUsers();
        
        for (User user : users) {
            try {
                // Check and award achievements based on current user statistics
                achievementService.checkAndAwardAchievements(user);
            } catch (Exception e) {
                System.err.println("Error awarding achievements for user " + user.getUsername() + ": " + e.getMessage());
            }
        }
        
        System.out.println("✅ User achievements awarded based on statistics");
    }

    // Helper class for question data
    private static class QuestionData {
        final String questionText;
        final QuestionType questionType;
        final List<String> options;
        final String correctAnswer;
        final int points;

        QuestionData(String questionText, QuestionType questionType, List<String> options, String correctAnswer, int points) {
            this.questionText = questionText;
            this.questionType = questionType;
            this.options = options;
            this.correctAnswer = correctAnswer;
            this.points = points;
        }
    }
}
