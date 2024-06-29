package com.example.train.service;

import com.example.train.entity.RoleName;
import com.example.train.entity.User;
import com.example.train.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Value("${myapp.passwordDb}")
    private String password;
    @Value("${myapp.adminName}")
    private String adminName;
    @Value("${myapp.adminEmail}")
    private String adminEmail;
    @Autowired
    private AuthService authService;

    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByUsername(adminName).isEmpty()) {
                User admin = new User();
                admin.setUsername(adminName);
                admin.setEmail(adminEmail);
                admin.setPassword(password);
                authService.register(admin, RoleName.ROLE_ADMIN);
                System.out.println("Админ создан");
            }
        };
    }
}