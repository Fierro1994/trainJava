package com.example.train.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetMail(String toEmail, String token) {
        String url = "http://localhost:8080/resetPassword?token=" + token;
        String subject = "Сброс пароля";
        String message = "Для сброса пароля перейдите по ссылке: " + url;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(toEmail);
        email.setSubject(subject);
        email.setText(message);
        email.setFrom(fromEmail);

        mailSender.send(email);
    }
}