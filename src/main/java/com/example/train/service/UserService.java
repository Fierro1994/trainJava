package com.example.train.service;

import com.example.train.entity.RoleName;
import com.example.train.entity.User;
import com.example.train.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if (!user.isPresent()) {
            throw new UsernameNotFoundException("User not found");
        }
        return new CustomUserDetails(user.get());
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User findByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return userRepository.findByUsername(username).get();
        }
        else return null;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }


    public void addPoints(User user, int points) {
        user.setPoints(user.getPoints() + points);
        userRepository.save(user);
    }

    public List<User> getTopUsers(int limit) {
        return userRepository.findTopUsers(PageRequest.of(0, limit));
    }

    public void changeUserRole(Long userId, RoleName role) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setRole(role);
        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        userRepository.delete(user);
    }
}