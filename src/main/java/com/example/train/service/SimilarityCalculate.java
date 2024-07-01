package com.example.train.service;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;

@Service
public class SimilarityCalculate {

    public double getTSimilarity(String text1, String text2) {
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        int distance = levenshtein.apply(text1, text2);
        int maxLength = Math.max(text1.length(), text2.length());

        double similarity = (1.0 - (double) distance / maxLength) * 100;

        return Math.max(0, Math.min(1, similarity / 100));
    }
}
