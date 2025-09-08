package com.qwizz.controller;

import com.qwizz.model.Quiz;
import com.qwizz.model.Question;
import com.qwizz.model.QuestionType;
import com.qwizz.model.Difficulty;
import com.qwizz.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/quiz")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @GetMapping("/browse")
    public String browseQuizzes(Model model,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String search) {
        List<Quiz> quizzes;

        if (search != null && !search.trim().isEmpty()) {
            quizzes = quizService.searchQuizzes(search.trim());
            model.addAttribute("searchTerm", search);
        } else if (difficulty != null && !difficulty.trim().isEmpty()) {
            try {
                Difficulty difficultyEnum = Difficulty.valueOf(difficulty.toUpperCase());
                quizzes = quizService.getQuizzesByDifficulty(difficultyEnum);
                model.addAttribute("selectedDifficulty", difficulty);
            } catch (IllegalArgumentException e) {
                quizzes = quizService.getPublicQuizzes();
            }
        } else {
            quizzes = quizService.getPublicQuizzes();
        }

        model.addAttribute("quizzes", quizzes);
        model.addAttribute("pageTitle", "Browse Quizzes - QWIZZ");
        return "quiz/browse";
    }

    @GetMapping("/create")
    public String createQuizPage(Model model, HttpSession session) {
        if (session.getAttribute("isLoggedIn") == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("pageTitle", "Create Quiz - QWIZZ");
        model.addAttribute("quiz", new Quiz());
        return "quiz/create";
    }

    @PostMapping("/create")
    public String createQuiz(@ModelAttribute Quiz quiz,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (session.getAttribute("isLoggedIn") == null) {
            return "redirect:/auth/login";
        }

        try {
            Long userId = (Long) session.getAttribute("userId");
            quiz.setCreatorId(userId);

            Quiz savedQuiz = quizService.createQuiz(quiz);
            redirectAttributes.addFlashAttribute("successMessage", "Quiz created successfully!");
            return "redirect:/quiz/edit/" + savedQuiz.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create quiz: " + e.getMessage());
            return "redirect:/quiz/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String editQuizPage(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("isLoggedIn") == null) {
            return "redirect:/auth/login";
        }

        Long userId = (Long) session.getAttribute("userId");

        if (!quizService.canUserEditQuiz(id, userId)) {
            return "redirect:/dashboard";
        }

        Optional<Quiz> quizOpt = quizService.findById(id);
        if (quizOpt.isEmpty()) {
            return "redirect:/dashboard";
        }

        Quiz quiz = quizOpt.get();
        List<Question> questions = quizService.getQuestionsByQuizId(id);

        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        model.addAttribute("newQuestion", new Question());
        model.addAttribute("pageTitle", "Edit Quiz: " + quiz.getTitle());

        return "quiz/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateQuiz(@PathVariable Long id,
            @ModelAttribute Quiz quiz,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (session.getAttribute("isLoggedIn") == null) {
            return "redirect:/auth/login";
        }

        Long userId = (Long) session.getAttribute("userId");

        if (!quizService.canUserEditQuiz(id, userId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to edit this quiz");
            return "redirect:/dashboard";
        }

        try {
            quiz.setId(id);
            quizService.updateQuiz(quiz);
            redirectAttributes.addFlashAttribute("successMessage", "Quiz updated successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update quiz: " + e.getMessage());
        }

        return "redirect:/quiz/edit/" + id;
    }

    @PostMapping("/edit/{id}/add-question")
    public String addQuestion(@PathVariable Long id,
            @ModelAttribute Question question,
            @RequestParam(required = false) String option1,
            @RequestParam(required = false) String option2,
            @RequestParam(required = false) String option3,
            @RequestParam(required = false) String option4,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (session.getAttribute("isLoggedIn") == null) {
            return "redirect:/auth/login";
        }

        Long userId = (Long) session.getAttribute("userId");

        if (!quizService.canUserEditQuiz(id, userId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to edit this quiz");
            return "redirect:/dashboard";
        }

        try {
            // Set up options for multiple choice questions
            if (QuestionType.MULTIPLE_CHOICE.equals(question.getQuestionType())) {
                List<String> options = List.of(option1, option2, option3, option4);
                question.setOptionsList(options);
            }

            quizService.addQuestionToQuiz(id, question);
            redirectAttributes.addFlashAttribute("successMessage", "Question added successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add question: " + e.getMessage());
        }

        return "redirect:/quiz/edit/" + id;
    }

    @GetMapping("/ai-generate")
    public String aiGeneratePage(Model model, HttpSession session) {
        if (session.getAttribute("isLoggedIn") == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("pageTitle", "AI Quiz Generator - QWIZZ");
        return "quiz/ai-generate";
    }

    @PostMapping("/ai-generate")
    public String generateAIQuiz(@RequestParam String topic,
            @RequestParam String difficulty,
            @RequestParam int questionCount,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (session.getAttribute("isLoggedIn") == null) {
            return "redirect:/auth/login";
        }

        try {
            Long userId = (Long) session.getAttribute("userId");
            Difficulty difficultyEnum = Difficulty.valueOf(difficulty.toUpperCase());
            Quiz generatedQuiz = quizService.generateAIQuiz(topic, difficultyEnum, questionCount, userId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "AI Quiz generated successfully! You can now review and edit it.");
            return "redirect:/quiz/edit/" + generatedQuiz.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to generate AI quiz: " + e.getMessage());
            return "redirect:/quiz/ai-generate";
        }
    }

    @GetMapping("/{id}")
    public String viewQuiz(@PathVariable Long id, Model model) {
        Optional<Quiz> quizOpt = quizService.findById(id);
        if (quizOpt.isEmpty()) {
            return "redirect:/quiz/browse";
        }

        Quiz quiz = quizOpt.get();
        List<Question> questions = quizService.getQuestionsByQuizId(id);

        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        model.addAttribute("pageTitle", quiz.getTitle() + " - QWIZZ");

        return "quiz/view";
    }

    @PostMapping("/delete/{id}")
    public String deleteQuiz(@PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (session.getAttribute("isLoggedIn") == null) {
            return "redirect:/auth/login";
        }

        Long userId = (Long) session.getAttribute("userId");

        if (!quizService.canUserEditQuiz(id, userId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to delete this quiz");
            return "redirect:/dashboard";
        }

        try {
            quizService.deleteQuiz(id);
            redirectAttributes.addFlashAttribute("successMessage", "Quiz deleted successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete quiz: " + e.getMessage());
        }

        return "redirect:/dashboard";
    }
}
