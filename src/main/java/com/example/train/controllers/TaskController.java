package com.example.train.controllers;

import com.example.train.entity.CategoryNames;
import com.example.train.entity.Task;
import com.example.train.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

@Controller
public class TaskController {
    @Autowired
    private TaskService taskService;

    @GetMapping("/tasks/add")
    public String addTask(Model model) {
        Task task = new Task();
        model.addAttribute("task", task);
        return "addTask";
    }

    @PostMapping("/tasks/save")
    public String save(@RequestParam String question, String answer, String theory, CategoryNames category, Model model) throws MalformedURLException, URISyntaxException {
        return taskService.saveTask(question, answer, theory, category, model);
    }

    @GetMapping("/tasks/{id}")
    public String task(@PathVariable Long id, Model model) {
        return taskService.getTask(id, model);
    }

    // Add the deleteTask method here
    @PostMapping("/tasks/{id}")
    public String deleteTask(@PathVariable Long id) {
        return taskService.deleteTask(id);
    }

    @GetMapping("/")
    public String getAllTasks(Model model) {
        List<Task> tasks = taskService.getAllTasks();
        model.addAttribute("tasks", tasks);
        return "tasksList";
    }

    @GetMapping("/tasks/random")
    public Task getRandomTask() {
        return taskService.getRandomTask();
    }

    @PostMapping("/tasks/checkAnswer")
    public String checkAnswer(@RequestParam String answer, Model model) {
        return taskService.checkAnswer(answer, model);
    }
}