package com.example.train.controllers;

import com.example.train.entity.CategoryNames;
import com.example.train.entity.Task;
import com.example.train.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @Secured({"ROLE_MODERATOR", "ROLE_ADMIN"})
    @GetMapping("/{id}/edit")
    public String editTask(@PathVariable Long id, Model model) {
        Task task = taskService.getTaskById(id);
        if (task == null) {
            return "redirect:/tasks/list";
        }
        model.addAttribute("task", task);
        return "addTask"; // Используйте тот же шаблон, что и для добавления задачи
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long id,
                       @RequestParam String question,
                       @RequestParam(required = false) String answer,
                       @RequestParam String theory,
                       @RequestParam CategoryNames category,
                       @RequestParam boolean isMultipleChoice,
                       @RequestParam(required = false) List<String> options,
                       @RequestParam(required = false) List<Integer> correctOptions,
                       Model model) throws Exception {
        if (id == null) {
            return taskService.saveTask(question, answer, theory, category, isMultipleChoice, options, correctOptions, model);
        } else {
            return taskService.updateTask(id, question, answer, theory, category, isMultipleChoice, options, correctOptions, model);
        }
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

    @GetMapping("/search")
    public String searchTasks(@RequestParam(required = false) String search,
                              @RequestParam(required = false) CategoryNames category,
                              @RequestParam(required = false) String questionType,
                              Model model) {
        List<Task> tasks = taskService.searchTasks(search, category, questionType);
        model.addAttribute("tasks", tasks);
        return "searchResults";
    }
}