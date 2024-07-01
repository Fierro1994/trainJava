package com.example.train.service;

import com.example.train.entity.CategoryNames;
import com.example.train.entity.Task;
import com.example.train.entity.TaskLog;
import com.example.train.entity.User;
import com.example.train.repos.TaskLogRepository;
import com.example.train.repos.TasksRepos;
import com.example.train.repos.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TaskService {
    private final double SIMILARITY_THRESHOLD = 0.8;


    @Autowired
    private TasksRepos tasksRepos;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskLogRepository taskLogRepository;
    @Autowired
    private SimilarityCalculate similarityCalculate;


    @Transactional(rollbackOn = Exception.class)
    public String saveTask(String question, String answer, String theory, CategoryNames category, boolean isMultipleChoice, List<String> options, List<Integer> correctOptionIndexes, Model model) throws Exception {
        Task task = new Task();
        task.setQuestion(question);
        task.setAnswer(answer);
        task.setCategory(category);
        task.setTheory(theory);
        task.setMultipleChoice(isMultipleChoice);

        if (options != null) {
            task.setOptions(options);
        }
        if (isMultipleChoice && correctOptionIndexes != null) {
            task.setCorrectOptionIndexes(new HashSet<>(correctOptionIndexes));
        } else if (!isMultipleChoice && correctOptionIndexes != null && !correctOptionIndexes.isEmpty()) {
            task.setCorrectOptionIndexes(Set.of(correctOptionIndexes.get(0)));
        }

        Task isSimilarTaskExists = isSimilarTaskExists(question);
        if (isSimilarTaskExists != null) {
            model.addAttribute("errorMessage", "Задача с похожим вопросом уже есть ");
            model.addAttribute("task", isSimilarTaskExists);
            return "addTask"; // Redirect to the addTask page with the error message
        }

        tasksRepos.save(task);

        logTaskAction("Добавление", task.getId());

        return "redirect:/tasks/" + task.getId();
    }

    public List<Task> searchTasks(String search, CategoryNames category, String questionType) {
        Stream<Task> taskStream = tasksRepos.findAll().stream();

        if (search != null && !search.isEmpty()) {
            taskStream = taskStream.filter(task -> task.getQuestion().toLowerCase().contains(search.toLowerCase()));
        }

        if (category != null) {
            taskStream = taskStream.filter(task -> task.getCategory().equals(category));
        }

        if (questionType != null && !questionType.isEmpty()) {
            if (questionType.equals("multipleChoice")) {
                taskStream = taskStream.filter(Task::isMultipleChoice);
            } else if (questionType.equals("text")) {
                taskStream = taskStream.filter(task -> !task.isMultipleChoice());
            }
        }

        return taskStream.collect(Collectors.toList());
    }

    public String getTask(Long id, Model model) {
        Task task = tasksRepos.findById(id).orElse(null);
        if (task == null) {
            return "redirect:/";
        }
        model.addAttribute("task", task);
        return "task";
    }

    public List<Task> getTasksByCategory(CategoryNames category) {
        return tasksRepos.findByCategory(category);
    }

    public String deleteTask(Long id) {
        Task task = tasksRepos.findById(id).orElse(null);
        deleteTaskForReview(task);
        tasksRepos.delete(task);

        logTaskAction("Удаление", id);

        return "redirect:/tasks";
    }

    private void logTaskAction(String action, Long taskId) {
        String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        TaskLog taskLog = new TaskLog();
        taskLog.setUsername(username);
        taskLog.setAction(action);
        taskLog.setTaskId(taskId);
        taskLog.setTimestamp(LocalDateTime.now());
        taskLogRepository.save(taskLog);
    }


    public void deleteTaskForReview(Task task) {
        Set<User> users = task.getUsers();
        for (User user : users) {
            user.getTasksForReview().remove(task);
            userRepository.save(user);
        }
    }

    public List<Task> getAllTasks() {
        return tasksRepos.findAll();
    }


    public Task isSimilarTaskExists(String question) throws Exception {
        List<Task> tasks = tasksRepos.findAll();
        for (Task existingTask : tasks) {
            String existingQuestion = existingTask.getQuestion();
            double similarity = similarityCalculate.getTSimilarity(question, existingQuestion);
            if (similarity >= SIMILARITY_THRESHOLD) {
                return existingTask;
            }
        }
        return null;
    }


}