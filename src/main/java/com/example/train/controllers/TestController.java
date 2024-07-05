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
        testService.initializeTest(numberOfQuestions, category, questionType);
        return testService.getTestPage(currentUser, model, timePerQuestion, category, questionType, numberOfQuestions);
    }

    @PostMapping("/submit")
    public String submitAnswer(@AuthenticationPrincipal UserDetails currentUser,
                               @RequestParam("taskId") Long taskId,
                               @RequestParam(value = "combinedAnswer", required = false) String combinedAnswer,
                               @RequestParam(required = false) Integer timePerQuestion,
                               @RequestParam(required = false) CategoryNames category,
                               @RequestParam int numberOfQuestions,
                               @RequestParam(required = false) String questionType,
                               Model model) {
        try {
            return testService.submitAnswer(taskId, combinedAnswer, model, currentUser, timePerQuestion, category, questionType, numberOfQuestions);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка отправки ответа: " + e.getMessage());
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