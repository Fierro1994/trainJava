
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
    private final double SIMILARITY_TEST_ANSWER = 0.8;
    private List<Task> questions = new ArrayList<>();
    private List<Map<String, Object>> userAnswers = new ArrayList<>();
    private Integer countQuestions;
    private int correctCount = 0;
    private List<Task> availableQuestions = new ArrayList<>();
    @Autowired
    private TasksRepos taskRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SimilarityCalculate similarityCalculate;
    @Autowired
    private UserService userService;

    public void initializeTest(Integer numberOfQuestions) {
        correctCount = 0;
        userAnswers.clear();
        questions =  taskRepository.findAll();
        availableQuestions = new ArrayList<>(questions);
        if (numberOfQuestions == null || numberOfQuestions <= 0) {
            countQuestions = questions.size();
        } else {
            countQuestions = Math.min(numberOfQuestions, questions.size());
            Collections.shuffle(availableQuestions);
            availableQuestions = availableQuestions.subList(0, countQuestions);
        }
    }

    public String getTestPage(UserDetails currentUser, Model model, Integer timePerQuestion, CategoryNames category, String questionType, Integer numberOfQuestions) {
        int availableQuestions = getAvailableQuestionsCount(category, questionType);

        if (numberOfQuestions == null || numberOfQuestions <= 0 || numberOfQuestions > availableQuestions) {
            numberOfQuestions = availableQuestions;
        }

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

    public int getAvailableQuestionsCount(CategoryNames category, String questionType) {
        Stream<Task> taskStream = taskRepository.findAll().stream();

        if (category != null) {
            taskStream = taskStream.filter(task -> task.getCategory().equals(category));
        }

        if (questionType != null && !questionType.isEmpty()) {
            if (questionType.equals("multipleChoice")) {
                taskStream = taskStream.filter(Task::isMultipleChoice);
            } else {
                taskStream = taskStream.filter(task -> !task.isMultipleChoice());
            }
        }

        return (int) taskStream.count();
    }



    private Task getNextQuestion(CategoryNames category, String questionType) {
        if (userAnswers.size() >= countQuestions) {
            return null;
        }

        List<Task> filteredTasks = availableQuestions.stream()
                .filter(task -> (category == null || task.getCategory().equals(category)) &&
                        (questionType == null || questionType.isEmpty() || task.isMultipleChoice() == "multipleChoice".equals(questionType)) &&
                        userAnswers.stream().noneMatch(answer -> task.getQuestion().equals(answer.get("question"))))
                .toList();

        if (filteredTasks.isEmpty()) {
            return null;
        }

        return filteredTasks.get(new Random().nextInt(filteredTasks.size()));
    }


    public String submitAnswer(Long taskId, String answer, Model model, UserDetails currentUser, Integer timePerQuestion, CategoryNames category, String questionType, Integer numberOfQuestions) throws Exception {
        Task currentTask = taskRepository.findById(taskId).orElse(null);
        if (currentTask == null) {
            model.addAttribute("errorMessage", "Task not found.");
            return "error";
        }
        boolean isCorrect;
        Map<String, Object> answerInfo = new HashMap<>();
        if (currentTask.isMultipleChoice()) {
            List<String> userAnswersList = Arrays.asList(answer.split(","));
            Set<String> correctAnswers = new HashSet<>();
            for (Integer index : currentTask.getCorrectOptionIndexes()) {
                correctAnswers.add(currentTask.getOptions().get(index));
            }
            Set<String> userAnswersSet = new HashSet<>(userAnswersList);
            isCorrect = correctAnswers.equals(userAnswersSet);

            int correctCount = 0;
            for (String userAnswer : userAnswersSet) {
                if (correctAnswers.contains(userAnswer)) {
                    correctCount++;
                }
            }
            double similarity = (double) correctCount / Math.max(correctAnswers.size(), userAnswersSet.size());

            List<Map<String, Object>> optionsInfo = new ArrayList<>();
            for (String option : currentTask.getOptions()) {
                Map<String, Object> optionInfo = new HashMap<>();
                optionInfo.put("text", option);
                optionInfo.put("isCorrect", correctAnswers.contains(option));
                optionInfo.put("isSelected", userAnswersSet.contains(option));
                optionsInfo.add(optionInfo);
            }

            answerInfo.put("optionsInfo", optionsInfo);
            answerInfo.put("isCorrect", isCorrect);
            answerInfo.put("similarity", similarity * 100); // в процентах
        } else {
            double similarity;
            try {
                similarity = similarityCalculate.getTSimilarity(answer, currentTask.getAnswer());
                isCorrect = isAnswerCorrect(answer, currentTask.getAnswer());
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Error calculating similarity.");
                return "error";
            }
            Map<String, String> highlightedTexts = getHighlightedText(currentTask.getAnswer(), answer);
            answerInfo.put("highlightedCorrectAnswer", highlightedTexts.get("highlightedCorrect"));
            answerInfo.put("highlightedUserAnswer", highlightedTexts.get("highlightedUser"));
            answerInfo.put("correctAnswer", currentTask.getAnswer());
            answerInfo.put("similarity", similarity * 100);
        }
        answerInfo.put("task", currentTask);
        answerInfo.put("question", currentTask.getQuestion());
        answerInfo.put("userAnswer", answer);
        answerInfo.put("taskId", currentTask.getId());
        userAnswers.add(answerInfo);
        Optional<User> user = userRepository.findByUsername(currentUser.getUsername());
        if (user.isPresent()) {
            if (isCorrect) {
                correctCount++;
                recordTestAttempt(user.get(), true, currentTask);
                userService.addPoints(user.get(), 2); // добавляем 2 очка за правильный ответ
            } else {
                recordTestAttempt(user.get(), false, currentTask);
            }
        }

        return getTestPage(currentUser, model, timePerQuestion, category, questionType, numberOfQuestions);
    }

    public Map<String, String> getHighlightedText(String correctAnswer, String userAnswer) throws Exception {
        List<String> correctWords = Arrays.asList(correctAnswer.split("\\s+"));
        List<String> userWords = Arrays.asList(userAnswer.split("\\s+"));

        StringBuilder highlightedCorrect = new StringBuilder();
        StringBuilder highlightedUser = new StringBuilder();

        for (String word : correctWords) {
            String lowercaseWord = word.toLowerCase();
            if (userWords.stream().anyMatch(w -> similarityCalculate.getTSimilarity(w.toLowerCase(), lowercaseWord) >= SIMILARITY_TEST_ANSWER)) {
                appendGreen(highlightedCorrect, word);
            } else {
                appendRed(highlightedCorrect, word);
            }
            highlightedCorrect.append(" ");
        }

        for (String word : userWords) {
            String lowercaseWord = word.toLowerCase();
            if (correctWords.stream().anyMatch(w -> similarityCalculate.getTSimilarity(w.toLowerCase(), lowercaseWord) >= SIMILARITY_TEST_ANSWER)) {
                appendGreen(highlightedUser, word);
            } else {
                appendRed(highlightedUser, word);
            }
            highlightedUser.append(" ");
        }

        Map<String, String> result = new HashMap<>(2);
        result.put("highlightedCorrect", highlightedCorrect.toString().trim());
        result.put("highlightedUser", highlightedUser.toString().trim());
        return result;
    }

    private void appendGreen(StringBuilder sb, String word) {
        sb.append("<span style='color: green;'>").append(word).append("</span>");
    }

    private void appendRed(StringBuilder sb, String word) {
        sb.append("<span style='color: red;'>").append(word).append("</span>");
    }


    public boolean isAnswerCorrect(String userAnswer, String correctAnswer) throws Exception {
        double similarity = similarityCalculate.getTSimilarity(userAnswer, correctAnswer);
        return similarity >= SIMILARITY_TEST_ANSWER;
    }


    private void recordTestAttempt(User user, boolean isCorrect, Task task) {
        Set<Task> tasksForReview = user.getTasksForReview();
        if (isCorrect) {
            if (tasksForReview.contains(task)) {
                deleteTaskForReview(task);
            }
            user.setCorrectAnswers(user.getCorrectAnswers() + 1);
        } else {
            if (!tasksForReview.contains(task)) {
                tasksForReview.add(task);
                user.setTasksForReview(tasksForReview);
            }
            user.setIncorrectAnswers(user.getIncorrectAnswers() + 1);
        }
        userRepository.save(user);
    }

    private void deleteTaskForReview(Task task) {
        Set<User> users = task.getUsers();
        for (User user : users) {
            user.getTasksForReview().remove(task);
            userRepository.save(user);
        }
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
        }
        return "test-summary";
    }

}
