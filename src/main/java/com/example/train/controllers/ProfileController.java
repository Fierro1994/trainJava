package com.example.train.controllers;

import com.example.train.models.ChangePasswordForm;
import com.example.train.entity.User;
import com.example.train.models.ForgotPasswordForm;
import com.example.train.models.ResetPasswordForm;
import com.example.train.service.AuthService;
import com.example.train.service.MailService;
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

import java.util.Optional;
import java.util.UUID;

@Controller
public class ProfileController {
    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;
    @Autowired
    private MailService mailService;

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        User user = userService.findByUsername(currentUser.getUsername());
        model.addAttribute("user", user);
        model.addAttribute("tasksForReview", user.getTasksForReview());

        return "profile";
    }

    @GetMapping("/profile/changePassword")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        return "changePassword";
    }

    @PostMapping("/profile/changePassword")
    public String changePassword(@AuthenticationPrincipal UserDetails currentUser, @ModelAttribute ChangePasswordForm form, Model model) {
        User user = userService.findByUsername(currentUser.getUsername());
        if (authService.checkIfValidOldPassword(user, form.getOldPassword())) {
            authService.changeUserPassword(user, form.getNewPassword());
            model.addAttribute("successMessage", "Пароль успешно изменен");
            return "changePassword";
        } else {
            model.addAttribute("errorMessage", "Неверный старый пароль");
            return "changePassword";
        }
    }


    @GetMapping("/forgotPassword")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("forgotPasswordForm", new ForgotPasswordForm());
        return "forgotPassword";
    }

    @PostMapping("/forgotPassword")
    public String forgotPassword(@ModelAttribute ForgotPasswordForm form, Model model) {
        User user = userService.findByEmail(form.getEmail());
        if (user != null) {
            String token = UUID.randomUUID().toString();
            authService.createPasswordResetTokenForUser(user, token);
            mailService.sendPasswordResetMail(user.getEmail(), token);
            model.addAttribute("message", "Инструкции по сбросу пароля отправлены на ваш email.");
            return "forgotPassword";
        } else {
            model.addAttribute("errorMessage", "Email не найден");
            return "forgotPassword";
        }
    }

    @GetMapping("/resetPassword")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        String result = authService.validatePasswordResetToken(token);
        if (result != null) {
            model.addAttribute("errorMessage", "Invalid token");
            return "login";
        } else {
            model.addAttribute("token", token);
            return "resetPassword.html";
        }
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestParam("token") String token, @ModelAttribute ResetPasswordForm form, Model model) {
        String result = authService.validatePasswordResetToken(token);
        if (result != null) {
            model.addAttribute("errorMessage", "Invalid token");
            return "login";
        } else {
            Optional<User> user = authService.getUserByPasswordResetToken(token);
            if (user.isPresent()) {
                authService.changeUserPassword(user.get(), form.getNewPassword());
                model.addAttribute("message", "Пароль успешно изменен");
                return "login";
            } else {
                model.addAttribute("errorMessage", "Invalid token");
                return "login";
            }
        }
    }
}