package com.example.train.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MvcMainController {

    @GetMapping("/")
    public String redirectToTasks() {
        return "redirect:/tasks/list";
    }
}
