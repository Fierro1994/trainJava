package com.example.train.service;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class SimilarityCalculate {

    public double getTSimilarity(String userAnswer, String correctAnswer) {

        List<String> userWords = Arrays.asList(userAnswer.toLowerCase().split("\\s+"));
        List<String> correctWords = Arrays.asList(correctAnswer.toLowerCase().split("\\s+"));


        Collections.sort(userWords);
        Collections.sort(correctWords);

        System.out.println(userWords);
        System.out.println(correctWords);

        String sortedUserAnswer = String.join(" ", userWords);
        String sortedCorrectAnswer = String.join(" ", correctWords);


        LevenshteinDistance levenshtein = new LevenshteinDistance();
        int distance = levenshtein.apply(sortedUserAnswer, sortedCorrectAnswer);
        int maxLength = Math.max(sortedUserAnswer.length(), sortedCorrectAnswer.length());

        double similarity = (1.0 - (double) distance / maxLength) * 100;

        return Math.max(0, Math.min(1, similarity / 100));
    }
}
