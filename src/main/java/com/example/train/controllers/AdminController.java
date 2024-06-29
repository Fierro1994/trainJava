package com.example.train.controllers;

import com.example.train.entity.RoleName;
import com.example.train.entity.TaskLog;
import com.example.train.entity.User;
import com.example.train.service.LogService;
import com.example.train.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private LogService taskLogService;

    @GetMapping("/admin/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/admin/logs")
    public String getTaskLogs(Model model) {
        List<TaskLog> logs = taskLogService.getAllLogs();
        model.addAttribute("logs", logs);
        return "admin/taskLogs";
    }

    @PostMapping("/admin/users/role")
    public String changeUserRole(@RequestParam Long userId, @RequestParam RoleName role) {
        userService.changeUserRole(userId, role);
        return "redirect:/admin/users";
    }
    @PostMapping("/admin/users/delete")
    public String deleteUser(@RequestParam Long userId) {
        userService.deleteUser(userId);
        return "redirect:/admin/users";
    }
}