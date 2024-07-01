package com.example.train.controllers;

import com.example.train.entity.User;
import com.example.train.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
@Controller
@RequestMapping("/users")
public class UsersController {
    @Autowired
    private UserService userService;
    @GetMapping("/top")
    public String getTopUsers(Model model) {
        List<User> topUsers = userService.getTopUsers(10); // получаем топ-10 пользователей
        model.addAttribute("topUsers", topUsers);
        if (!topUsers.isEmpty()) {
            model.addAttribute("firstUser", topUsers.get(0));
        }
        return "topUsers";
    }

    @GetMapping("/{id}")
    public String viewUserProfile(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        if (user == null) {
            return "redirect:/top";
        }
        model.addAttribute("user", user);

        double correctAnswers = user.getCorrectAnswers();
        double totalAnswers = correctAnswers + user.getIncorrectAnswers();
        double correctPercentage = (totalAnswers > 0) ? (correctAnswers / totalAnswers) * 100 : 0;
        String formattedCorrectPercentage = String.format("%.2f", correctPercentage);
        model.addAttribute("correctPercentage", formattedCorrectPercentage);

        return "viewUserProfile";
    }
}
