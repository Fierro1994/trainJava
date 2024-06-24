package com.example.train.controllers;

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

    @GetMapping
    public String getTestPage(Model model) {
        try {
            testService.initializeTest();
            return testService.getTestPage(model);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка загрузки теста");
            return "error";
        }
    }

    @PostMapping("/submit")
    public String submitAnswer(@AuthenticationPrincipal UserDetails currentUser,
                               @RequestParam("taskId") Long taskId,
                               @RequestParam("answer") String answer, Model model) {
        try {
            return testService.submitAnswer(taskId, answer, model, currentUser);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка отправки ответа");
            return "error";
        }
    }

    @GetMapping("/finish")
    public String finishTest(Model model) {
        try {
            return testService.finishTest(model);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка завершения теста");
            return "error";
        }
    }
}