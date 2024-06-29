package com.example.train.service;

import com.example.train.entity.PasswordResetToken;
import com.example.train.entity.RoleName;
import com.example.train.entity.User;
import com.example.train.models.ChangePasswordForm;
import com.example.train.models.ForgotPasswordForm;
import com.example.train.models.ResetPasswordForm;
import com.example.train.repos.PasswordResetTokenRepository;
import com.example.train.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    UserService userService;
    @Autowired
    private MailService mailService;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    public String getProfilePage(UserDetails currentUser, Model model){
        User user = userService.findByUsername(currentUser.getUsername());
        model.addAttribute("user", user);
        model.addAttribute("tasksForReview", user.getTasksForReview());
        model.addAttribute("roleDisplayName", user.getRole().getDisplayName());

        double correctAnswers = user.getCorrectAnswers();
        double totalAnswers = correctAnswers + user.getIncorrectAnswers();
        double correctPercentage = (totalAnswers > 0) ? (correctAnswers / totalAnswers) * 100 : 0;
        String formattedCorrectPercentage = String.format("%.2f", correctPercentage);

        model.addAttribute("correctPercentage", formattedCorrectPercentage);

        return "profile";
    }

    public String changePassword(UserDetails currentUser, ChangePasswordForm form, Model model){
        User user = userService.findByUsername(currentUser.getUsername());
        if (checkIfValidOldPassword(user, form.getOldPassword())) {
            changeUserPassword(user, form.getNewPassword());
            model.addAttribute("successMessage", "Пароль успешно изменен");
            return "changePassword";
        } else {
            model.addAttribute("errorMessage", "Неверный старый пароль");
            return "changePassword";
        }
    }

    public String forgotPassword(ForgotPasswordForm form, Model model){
        User user = userService.findByEmail(form.getEmail());
        if (user != null) {
            String token = UUID.randomUUID().toString();
            createPasswordResetTokenForUser(user, token);
            mailService.sendPasswordResetMail(user.getEmail(), token);
            model.addAttribute("message", "Инструкции по сбросу пароля отправлены на ваш email.");
            return "forgotPassword";
        } else {
            model.addAttribute("errorMessage", "Email не найден");
            return "forgotPassword";
        }
    }

    public String showResetPasswordForm( String token, Model model){
        String result = validatePasswordResetToken(token);
        if (result != null) {
            model.addAttribute("errorMessage", "Invalid token");
            return "login";
        } else {
            model.addAttribute("token", token);
            return "resetPassword.html";
        }
    }

    public String resetPassword(String token, ResetPasswordForm form, Model model){
        String result = validatePasswordResetToken(token);
        if (result != null) {
            model.addAttribute("errorMessage", "Invalid token");
            return "login";
        } else {
            Optional<User> user = getUserByPasswordResetToken(token);
            if (user.isPresent()) {
                changeUserPassword(user.get(), form.getNewPassword());
                model.addAttribute("message", "Пароль успешно изменен");
                return "login";
            } else {
                model.addAttribute("errorMessage", "Invalid token");
                return "login";
            }
        }
    }

    public void register(User user, RoleName role) {
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole(role);
        userRepository.save(user);
    }

    public void changeUserPassword(User user, String newPassword) {
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean checkIfValidOldPassword(User user, String oldPassword) {
        return encoder.matches(oldPassword, user.getPassword());
    }


    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken myToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(myToken);
    }

    public String validatePasswordResetToken(String token) {
        final PasswordResetToken passToken = passwordResetTokenRepository.findByToken(token);

        return !isTokenFound(passToken) ? "invalidToken"
                : isTokenExpired(passToken) ? "expired"
                : null;
    }

    private boolean isTokenFound(PasswordResetToken passToken) {
        return passToken != null;
    }

    private boolean isTokenExpired(PasswordResetToken passToken) {
        final Calendar cal = Calendar.getInstance();
        return passToken.getExpiryDate().before(cal.getTime());
    }

    public Optional<User> getUserByPasswordResetToken(String token) {
        return Optional.ofNullable(passwordResetTokenRepository.findByToken(token).getUser());
    }
}