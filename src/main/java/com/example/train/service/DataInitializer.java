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
    @Autowired
    private AuthService authService;

    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByUsername("fierro1994").isEmpty()) {
                User admin = new User();
                admin.setUsername("fierro1994");
                admin.setEmail("26roma261994@mail.ru");
                admin.setPassword(password);
                authService.register(admin, RoleName.ROLE_ADMIN);
                System.out.println("Админ создан");
            }
        };
    }
}