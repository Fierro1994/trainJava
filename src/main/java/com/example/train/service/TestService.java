package com.example.train.service;

import com.example.train.entity.Task;
import com.example.train.entity.User;
import com.example.train.repos.TasksRepos;
import com.example.train.repos.UserRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.*;

@Service
public class TestService {

    private List<Task> questions = new ArrayList<>();
    private List<Map<String, Object>> userAnswers = new ArrayList<>();
    private int correctCount = 0;
    @Autowired
    private TasksRepos taskRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskService taskService;

    public void initializeTest() {
        correctCount = 0;
        userAnswers.clear();
        taskService.resetUsedTasks();
        questions = taskService.getAllTasks();
    }

    public String getTestPage(Model model) {
        Task task = getNextQuestion();
        if (task == null) {
            return finishTest(model);
        }
        model.addAttribute("task", task);
        model.addAttribute("showResult", false);
        return "test";
    }

    public String submitAnswer(Long taskId, String answer, Model model, UserDetails currentUser) {
        Task currentTask = taskRepository.findById(taskId).orElse(null);
        if (currentTask == null) {
            model.addAttribute("errorMessage", "Task not found.");
            return "error";
        }

        double similarity = taskService.getSimilarityPercentage(answer, currentTask.getAnswer());
        boolean isCorrect = taskService.isAnswerCorrect(answer, currentTask.getAnswer());

        Map<String, Object> answerInfo = new HashMap<>();
        answerInfo.put("question", currentTask.getQuestion());
        answerInfo.put("similarity", similarity);
        answerInfo.put("correctAnswer", currentTask.getAnswer());
        answerInfo.put("userAnswer", answer);
        answerInfo.put("taskId", currentTask.getId());

        userAnswers.add(answerInfo);
        Optional<User> user = userRepository.findByUsername(currentUser.getUsername());
        if (isCorrect) {
            correctCount++;
            recordTestAttempt(user.get(), true, taskRepository.findById(taskId).get());
        } else {
            recordTestAttempt(user.get(), false, taskRepository.findById(taskId).get());
        }

        return getTestPage(model);
    }

    private void recordTestAttempt(User user, boolean isCorrect, Task task) {
        user.setTestAttempts(user.getTestAttempts() + 1);
        if (isCorrect) {
            user.setCorrectAnswers(user.getCorrectAnswers() + 1);
        } else {
            user.getTasksForReview().add(task);
        }
        userRepository.save(user);
    }

    private Task getNextQuestion() {
        for (Task task : questions) {
            if (!userAnswers.stream().anyMatch(answer -> task.getQuestion().equals(answer.get("question")))) {
                return task;
            }
        }
        return null;
    }

    public String finishTest(Model model) {
        model.addAttribute("correctCount", correctCount);
        model.addAttribute("totalQuestions", questions.size());
        model.addAttribute("userAnswers", userAnswers);
        taskService.resetUsedTasks();
        return "test-summary";
    }


}