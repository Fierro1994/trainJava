package com.example.train.controllers;

import com.example.train.entity.User;
import com.example.train.models.ChangePasswordForm;
import com.example.train.models.ForgotPasswordForm;
import com.example.train.models.ResetPasswordForm;
import com.example.train.service.AuthService;
import com.example.train.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
public class ProfileController {
    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;


    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal UserDetails currentUser, Model model) {
       return authService.getProfilePage(currentUser, model);
    }

    @GetMapping("/profile/changePassword")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        return "changePassword";
    }

    @PostMapping("/profile/changePassword")
    public String changePassword(@AuthenticationPrincipal UserDetails currentUser, @ModelAttribute ChangePasswordForm form, Model model) {
        return authService.changePassword(currentUser, form, model);
    }


    @GetMapping("/forgotPassword")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("forgotPasswordForm", new ForgotPasswordForm());
        return "forgotPassword";
    }

    @PostMapping("/forgotPassword")
    public String forgotPassword(@ModelAttribute ForgotPasswordForm form, Model model) {
        return authService.forgotPassword(form, model);
    }

    @GetMapping("/resetPassword")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
      return authService.showResetPasswordForm(token, model);
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestParam("token") String token, @ModelAttribute ResetPasswordForm form, Model model) {
      return authService.resetPassword(token, form, model);
    }

    @GetMapping("/top")
    public String getTopUsers(Model model) {
        List<User> topUsers = userService.getTopUsers(10); // получаем топ-10 пользователей
        model.addAttribute("topUsers", topUsers);
        if (!topUsers.isEmpty()) {
            model.addAttribute("firstUser", topUsers.get(0));
        }
        return "topUsers";
    }
}