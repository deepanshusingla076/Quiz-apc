package com.qwizz.controller;

import com.qwizz.model.*;
import com.qwizz.service.QuestionBankService;
import com.qwizz.service.QuizService;
import com.qwizz.service.ResultService;
import com.qwizz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Teacher-specific controller for quiz creation, question management, and result viewing
 */
@Controller
@RequestMapping("/teacher")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuestionBankService questionBankService;

    @Autowired
    private ResultService resultService;

    @Autowired
    private UserService userService;

    // Teacher Dashboard
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal User teacher, Model model) {
        ResultService.TeacherAnalytics analytics = resultService.getTeacherAnalytics(teacher.getId());
        List<Quiz> recentQuizzes = quizService.getQuizzesByCreator(teacher.getId());
        
        model.addAttribute("teacher", teacher);
        model.addAttribute("analytics", analytics);
        model.addAttribute("recentQuizzes", recentQuizzes);
        
        return "teacher/dashboard";
    }

    // Quiz Management
    @GetMapping("/quizzes")
    public String manageQuizzes(@AuthenticationPrincipal User teacher, Model model) {
        List<Quiz> quizzes = quizService.getQuizzesByCreator(teacher.getId());
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("teacher", teacher);
        return "teacher/quizzes";
    }

    @GetMapping("/quiz/create")
    public String createQuizForm(Model model) {
        model.addAttribute("quiz", new Quiz());
        model.addAttribute("difficulties", Difficulty.values());
        return "teacher/create-quiz";
    }

    @PostMapping("/quiz/create")
    public String createQuiz(@AuthenticationPrincipal User teacher, 
                           @ModelAttribute Quiz quiz) {
        quiz.setCreatorId(teacher.getId());
        Quiz savedQuiz = quizService.createQuiz(quiz);
        return "redirect:/teacher/quiz/" + savedQuiz.getId() + "/questions";
    }

    @GetMapping("/quiz/{id}/edit")
    public String editQuizForm(@PathVariable Long id, 
                              @AuthenticationPrincipal User teacher, 
                              Model model) {
        Optional<Quiz> quizOpt = quizService.getQuizById(id);
        if (quizOpt.isEmpty() || !quizOpt.get().getCreatorId().equals(teacher.getId())) {
            return "redirect:/teacher/quizzes?error=notfound";
        }
        
        model.addAttribute("quiz", quizOpt.get());
        model.addAttribute("difficulties", Difficulty.values());
        return "teacher/edit-quiz";
    }

    @PostMapping("/quiz/{id}/edit")
    public String editQuiz(@PathVariable Long id,
                          @AuthenticationPrincipal User teacher,
                          @ModelAttribute Quiz quiz) {
        Optional<Quiz> existingQuizOpt = quizService.getQuizById(id);
        if (existingQuizOpt.isEmpty() || !existingQuizOpt.get().getCreatorId().equals(teacher.getId())) {
            return "redirect:/teacher/quizzes?error=notfound";
        }
        
        quiz.setId(id);
        quiz.setCreatorId(teacher.getId());
        quizService.updateQuiz(quiz);
        return "redirect:/teacher/quiz/" + id;
    }

    @PostMapping("/quiz/{id}/delete")
    public String deleteQuiz(@PathVariable Long id, 
                            @AuthenticationPrincipal User teacher) {
        Optional<Quiz> quizOpt = quizService.getQuizById(id);
        if (quizOpt.isPresent() && quizOpt.get().getCreatorId().equals(teacher.getId())) {
            quizService.deleteQuiz(id);
        }
        return "redirect:/teacher/quizzes";
    }

    // Question Management
    @GetMapping("/quiz/{quizId}/questions")
    public String manageQuestions(@PathVariable Long quizId, 
                                 @AuthenticationPrincipal User teacher, 
                                 Model model) {
        Optional<Quiz> quizOpt = quizService.getQuizById(quizId);
        if (quizOpt.isEmpty() || !quizOpt.get().getCreatorId().equals(teacher.getId())) {
            return "redirect:/teacher/quizzes?error=notfound";
        }
        
        Quiz quiz = quizOpt.get();
        List<Question> questions = questionBankService.getQuestionsByQuizId(quizId);
        
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        return "teacher/manage-questions";
    }

    @GetMapping("/quiz/{quizId}/question/create")
    public String createQuestionForm(@PathVariable Long quizId, 
                                   @AuthenticationPrincipal User teacher, 
                                   Model model) {
        Optional<Quiz> quizOpt = quizService.getQuizById(quizId);
        if (quizOpt.isEmpty() || !quizOpt.get().getCreatorId().equals(teacher.getId())) {
            return "redirect:/teacher/quizzes?error=notfound";
        }
        
        Question question = new Question();
        question.setQuizId(quizId);
        
        model.addAttribute("quiz", quizOpt.get());
        model.addAttribute("question", question);
        model.addAttribute("questionTypes", QuestionType.values());
        return "teacher/create-question";
    }

    @PostMapping("/quiz/{quizId}/question/create")
    public String createQuestion(@PathVariable Long quizId,
                               @AuthenticationPrincipal User teacher,
                               @ModelAttribute Question question,
                               @RequestParam(required = false) List<String> options) {
        Optional<Quiz> quizOpt = quizService.getQuizById(quizId);
        if (quizOpt.isEmpty() || !quizOpt.get().getCreatorId().equals(teacher.getId())) {
            return "redirect:/teacher/quizzes?error=notfound";
        }
        
        question.setQuizId(quizId);
        if (options != null && !options.isEmpty()) {
            question.setOptionsList(options);
        }
        
        questionBankService.createQuestion(question);
        return "redirect:/teacher/quiz/" + quizId + "/questions";
    }

    @GetMapping("/question/{id}/edit")
    public String editQuestionForm(@PathVariable Long id, 
                                  @AuthenticationPrincipal User teacher, 
                                  Model model) {
        Optional<Question> questionOpt = questionBankService.getQuestionById(id);
        if (questionOpt.isEmpty()) {
            return "redirect:/teacher/quizzes?error=notfound";
        }
        
        Question question = questionOpt.get();
        Optional<Quiz> quizOpt = quizService.getQuizById(question.getQuizId());
        if (quizOpt.isEmpty() || !quizOpt.get().getCreatorId().equals(teacher.getId())) {
            return "redirect:/teacher/quizzes?error=notfound";
        }
        
        model.addAttribute("question", question);
        model.addAttribute("quiz", quizOpt.get());
        model.addAttribute("questionTypes", QuestionType.values());
        return "teacher/edit-question";
    }

    @PostMapping("/question/{id}/edit")
    public String editQuestion(@PathVariable Long id,
                              @AuthenticationPrincipal User teacher,
                              @ModelAttribute Question question,
                              @RequestParam(required = false) List<String> options) {
        Optional<Question> existingQuestionOpt = questionBankService.getQuestionById(id);
        if (existingQuestionOpt.isEmpty()) {
            return "redirect:/teacher/quizzes?error=notfound";
        }
        
        Question existingQuestion = existingQuestionOpt.get();
        Optional<Quiz> quizOpt = quizService.getQuizById(existingQuestion.getQuizId());
        if (quizOpt.isEmpty() || !quizOpt.get().getCreatorId().equals(teacher.getId())) {
            return "redirect:/teacher/quizzes?error=notfound";
        }
        
        if (options != null && !options.isEmpty()) {
            question.setOptionsList(options);
        }
        
        questionBankService.updateQuestion(id, question);
        return "redirect:/teacher/quiz/" + existingQuestion.getQuizId() + "/questions";
    }

    @PostMapping("/question/{id}/delete")
    public String deleteQuestion(@PathVariable Long id, 
                                @AuthenticationPrincipal User teacher) {
        Optional<Question> questionOpt = questionBankService.getQuestionById(id);
        if (questionOpt.isPresent()) {
            Question question = questionOpt.get();
            Optional<Quiz> quizOpt = quizService.getQuizById(question.getQuizId());
            if (quizOpt.isPresent() && quizOpt.get().getCreatorId().equals(teacher.getId())) {
                questionBankService.deleteQuestion(id);
                return "redirect:/teacher/quiz/" + question.getQuizId() + "/questions";
            }
        }
        return "redirect:/teacher/quizzes";
    }

    // Results and Analytics
    @GetMapping("/quiz/{id}/results")
    public String viewQuizResults(@PathVariable Long id, 
                                 @AuthenticationPrincipal User teacher, 
                                 Model model) {
        Optional<Quiz> quizOpt = quizService.getQuizById(id);
        if (quizOpt.isEmpty() || !quizOpt.get().getCreatorId().equals(teacher.getId())) {
            return "redirect:/teacher/quizzes?error=notfound";
        }
        
        Quiz quiz = quizOpt.get();
        ResultService.QuizAnalytics analytics = resultService.getQuizAnalytics(id);
        List<QuizAttempt> attempts = resultService.getQuizAttempts(id);
        List<ResultService.LeaderboardEntry> leaderboard = resultService.getQuizLeaderboard(id, 10);
        
        model.addAttribute("quiz", quiz);
        model.addAttribute("analytics", analytics);
        model.addAttribute("attempts", attempts);
        model.addAttribute("leaderboard", leaderboard);
        
        return "teacher/quiz-results";
    }

    @GetMapping("/students")
    public String viewStudents(@AuthenticationPrincipal User teacher, Model model) {
        List<User> students = userService.getUsersByRole(Role.STUDENT);
        model.addAttribute("students", students);
        model.addAttribute("teacher", teacher);
        return "teacher/students";
    }

    @GetMapping("/student/{id}/progress")
    public String viewStudentProgress(@PathVariable Long id, 
                                     @AuthenticationPrincipal User teacher, 
                                     Model model) {
        Optional<User> studentOpt = userService.findById(id);
        if (studentOpt.isEmpty() || !studentOpt.get().isStudent()) {
            return "redirect:/teacher/students?error=notfound";
        }
        
        User student = studentOpt.get();
        ResultService.StudentAnalytics analytics = resultService.getStudentAnalytics(id);
        List<QuizAttempt> attempts = resultService.getUserAttempts(id);
        
        model.addAttribute("student", student);
        model.addAttribute("analytics", analytics);
        model.addAttribute("attempts", attempts);
        
        return "teacher/student-progress";
    }

    // API Endpoints for AJAX calls
    @GetMapping("/api/quiz/{id}")
    @ResponseBody
    public ResponseEntity<Quiz> getQuizAPI(@PathVariable Long id, 
                                          @AuthenticationPrincipal User teacher) {
        Optional<Quiz> quizOpt = quizService.getQuizById(id);
        if (quizOpt.isEmpty() || !quizOpt.get().getCreatorId().equals(teacher.getId())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(quizOpt.get());
    }

    @GetMapping("/api/quiz/{id}/analytics")
    @ResponseBody
    public ResponseEntity<ResultService.QuizAnalytics> getQuizAnalyticsAPI(@PathVariable Long id, 
                                                                           @AuthenticationPrincipal User teacher) {
        Optional<Quiz> quizOpt = quizService.getQuizById(id);
        if (quizOpt.isEmpty() || !quizOpt.get().getCreatorId().equals(teacher.getId())) {
            return ResponseEntity.notFound().build();
        }
        
        ResultService.QuizAnalytics analytics = resultService.getQuizAnalytics(id);
        return ResponseEntity.ok(analytics);
    }
}
