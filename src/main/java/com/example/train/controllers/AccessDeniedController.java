package com.example.train.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccessDeniedController {


    @GetMapping("/accessDenied")
    public String accessDenied(Model model) {
        model.addAttribute("errorMessage", "У вас нет прав для добавления задач. Пожалуйста, обратитесь к администратору.");
        return "accessDenied";
    }
}