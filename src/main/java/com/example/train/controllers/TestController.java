package com.example.train.controllers;

import com.example.train.entity.CategoryNames;
import com.example.train.service.TaskService;
import com.example.train.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
@RequestMapping("/test")
public class TestController {
    @Autowired
    private TestService testService;
    @Autowired
    private TaskService taskService;

    @GetMapping
    public String testConfig(Model model) {
        return "testConfig";
    }

    @GetMapping("/start")
    public String startTest(@AuthenticationPrincipal UserDetails currentUser,
                            @RequestParam(required = false) Integer timePerQuestion,
                            @RequestParam(required = false) CategoryNames category,
                            @RequestParam(required = false) Integer numberOfQuestions,
                            @RequestParam(required = false) String questionType,
                            Model model) {
        int availableQuestions = taskService.getAvailableQuestionsCount(category, questionType);

        if (numberOfQuestions == null || numberOfQuestions <= 0 || numberOfQuestions > availableQuestions) {
            numberOfQuestions = availableQuestions;
        }

        testService.initializeTest(numberOfQuestions);
        return testService.getTestPage(currentUser, model, timePerQuestion, category, questionType, numberOfQuestions);
    }

    @PostMapping("/submit")
    public String submitAnswer(@AuthenticationPrincipal UserDetails currentUser,
                               @RequestParam("taskId") Long taskId,
                               @RequestParam(value = "answer", required = false) String answer,
                               @RequestParam(required = false) Integer timePerQuestion,
                               @RequestParam(required = false) CategoryNames category,
                               @RequestParam int numberOfQuestions,
                               @RequestParam(required = false) String questionType,
                               Model model) {
        try {
            return testService.submitAnswer(taskId, answer, model, currentUser, timePerQuestion, category, questionType, numberOfQuestions);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка отправки ответа");
            return "error";
        }
    }

    @GetMapping("/finish")
    public String finishTest(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        try {
            return testService.finishTest(model, currentUser);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка завершения теста");
            return "error";
        }
    }
}