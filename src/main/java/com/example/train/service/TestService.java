package com.example.train.service;

import com.example.train.entity.CategoryNames;
import com.example.train.entity.Task;
import com.example.train.entity.User;
import com.example.train.repos.TasksRepos;
import com.example.train.repos.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
public class TestService {

    private List<Task> questions = new ArrayList<>();
    private List<Map<String, Object>> userAnswers = new ArrayList<>();
    private Integer countQuestions;
    private int correctCount = 0;
    @Autowired
    private TasksRepos taskRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskService taskService;

    public void initializeTest(Integer numberOfQuestions) {
        correctCount = 0;
        userAnswers.clear();
        taskService.resetUsedTasks();
        questions = taskService.getAllTasks();
        if (numberOfQuestions == null || numberOfQuestions <= 0) {
            countQuestions = questions.size();
        } else {
            countQuestions = Math.min(numberOfQuestions, questions.size());
            Collections.shuffle(questions);
            questions = questions.subList(0, countQuestions);
        }
    }

    public String getTestPage(UserDetails currentUser, Model model, Integer timePerQuestion, CategoryNames category, String questionType, Integer numberOfQuestions) {
        Task task = getNextQuestion(category, questionType);

        if (task == null && userAnswers.isEmpty()) {
            return "noQuestions";
        } else if (task == null) {
            return finishTest(model, currentUser);
        }

        if (timePerQuestion == null) {
            timePerQuestion = 0;
        }

        model.addAttribute("task", task);
        model.addAttribute("timePerQuestion", timePerQuestion);
        model.addAttribute("category", category);
        model.addAttribute("questionType", questionType);
        model.addAttribute("currentQuestionNumber", userAnswers.size() + 1);
        model.addAttribute("totalQuestions", numberOfQuestions != null && numberOfQuestions > 0 ? numberOfQuestions : "∞");

        return "test";
    }

    private Task getNextQuestion(CategoryNames category, String questionType) {
        Stream<Task> availableQuestionsStream = questions.stream()
                .filter(task -> userAnswers.stream().noneMatch(answer -> task.getQuestion().equals(answer.get("question"))));

        if (category != null) {
            availableQuestionsStream = availableQuestionsStream
                    .filter(task -> task.getCategory().name().equals(category.name()));
        }

        if (questionType != null && !questionType.isEmpty()) {
            availableQuestionsStream = availableQuestionsStream
                    .filter(task -> "multipleChoice".equals(questionType) ? task.isMultipleChoice() : !task.isMultipleChoice());
        }

        List<Task> availableQuestions = availableQuestionsStream.toList();

        if (availableQuestions.isEmpty()) {
            return null;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(availableQuestions.size());
        return availableQuestions.get(randomIndex);
    }

    public String submitAnswer(Long taskId, String answer, Model model, UserDetails currentUser, Integer timePerQuestion, CategoryNames category, String questionType, Integer numberOfQuestions) {
        Task currentTask = taskRepository.findById(taskId).orElse(null);
        if (currentTask == null) {
            model.addAttribute("errorMessage", "Task not found.");
            return "error";
        }
        double similarity = 0.0;
        boolean isCorrect;
        Map<String, Object> answerInfo = new HashMap<>();
        if (currentTask.isMultipleChoice()) {
            Set<String> correctAnswers = new HashSet<>();
            StringBuilder stringBuilder = new StringBuilder();

                int count = 0;
                for (Integer index : currentTask.getCorrectOptionIndexes()) {
                    if (count > 0) {
                        stringBuilder.append(",");
                    }
                    stringBuilder.append(currentTask.getOptions().get(index));
                    count++;
                }

            correctAnswers.add(stringBuilder.toString());
            isCorrect = correctAnswers.contains(answer);

            answerInfo.put("correctAnswers", correctAnswers);
        } else {
            similarity = taskService.getSimilarityPercentage(answer, currentTask.getAnswer());
            isCorrect = taskService.isAnswerCorrect(answer, currentTask.getAnswer());
            answerInfo.put("correctAnswer", currentTask.getAnswer());
        }
        answerInfo.put("task", currentTask);
        answerInfo.put("question", currentTask.getQuestion());
        answerInfo.put("similarity", similarity);
        answerInfo.put("userAnswer", answer);
        answerInfo.put("taskId", currentTask.getId());
        userAnswers.add(answerInfo);
        Optional<User> user = userRepository.findByUsername(currentUser.getUsername());
        if (user.isPresent()) {
            if (isCorrect) {
                correctCount++;
                recordTestAttempt(user.get(), true, currentTask);
            } else {
                recordTestAttempt(user.get(), false, currentTask);
            }
        }

        return getTestPage(currentUser, model, timePerQuestion, category, questionType, numberOfQuestions);
    }

    private void recordTestAttempt(User user, boolean isCorrect, Task task) {
        Set<Task> tasksForReview = user.getTasksForReview();
        if (isCorrect) {
            if (tasksForReview.contains(task)) {
                taskService.deleteTaskForReview(task);
            }
            user.setCorrectAnswers(user.getCorrectAnswers() + 1);
        } else {
            if (!tasksForReview.contains(task)) {
                tasksForReview.add(task);
                user.setTasksForReview(tasksForReview);
            }
            user.setIncorrectAnswers(user.getIncorrectAnswers() + 1); // Увеличиваем количество неправильных ответов
        }
        userRepository.save(user); // Сохраняем изменения в базе данных
    }

    public String finishTest(Model model, UserDetails currentUser) {
        Optional<User> user = userRepository.findByUsername(currentUser.getUsername());
        if (user.isPresent()) {
            User userEntity = user.get();
            userEntity.setTestAttempts(userEntity.getTestAttempts() + 1);
            userRepository.save(userEntity);
            model.addAttribute("correctCount", correctCount);
            model.addAttribute("totalQuestions", countQuestions);
            model.addAttribute("userAnswers", userAnswers);
            taskService.resetUsedTasks();
        }
        return "test-summary";
    }
}