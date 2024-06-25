package com.example.train.controllers;

import com.example.train.entity.CategoryNames;
import com.example.train.entity.Task;
import com.example.train.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private TaskService taskService;



    @Secured({"ROLE_MODERATOR", "ROLE_ADMIN"})
    @GetMapping("/add")
    public String addTask(Model model) {
        Task task = new Task();
        model.addAttribute("task", task);
        return "addTask";
    }

    @PostMapping("/save")
    public String save(@RequestParam String question,
                       @RequestParam String answer,
                       @RequestParam String theory,
                       @RequestParam CategoryNames category,
                       Model model) throws MalformedURLException, URISyntaxException {
        return taskService.saveTask(question, answer, theory, category, model);
    }

    @GetMapping("/{id}")
    public String task(@PathVariable Long id, Model model) {
        return taskService.getTask(id, model);
    }

    @PostMapping("/{id}/delete")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "redirect:/tasks/list";
    }

    @GetMapping("/list")
    public String getAllTasks(Model model) {
        List<Task> tasks = taskService.getAllTasks();
        model.addAttribute("tasks", tasks);
        return "tasksList";
    }

    @GetMapping("/random")
    public Task getRandomTask() {
        return taskService.getRandomTask();
    }

    @PostMapping("/checkAnswer")
    public String checkAnswer(@RequestParam String answer, Model model) {
        return taskService.checkAnswer(answer, model);
    }
}