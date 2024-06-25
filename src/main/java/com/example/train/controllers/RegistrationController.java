package com.example.train.controllers;

import com.example.train.entity.RoleName;
import com.example.train.entity.User;
import com.example.train.service.AuthService;
import com.example.train.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {
    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        System.out.println(user.getUsername());
        if (userService.findByUsername(user.getUsername()) != null) {
            System.out.println("проверка");
            model.addAttribute("errorMessage", "Username already exists.");
            return "register";
        }


        System.out.println("дальше");
        authService.register(user, RoleName.ROLE_USER);
        return "redirect:/login";
    }
}