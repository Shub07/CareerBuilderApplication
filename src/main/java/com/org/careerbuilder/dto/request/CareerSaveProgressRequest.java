package com.org.careerbuilder.dto.request;

import java.util.Map;

/**
 * Save progress as student navigates through questions.
 * answers: map of questionOrder (0-based) -> selectedOptionIndex
 */
public record CareerSaveProgressRequest(
        Map<Integer, Integer> answers,
        Integer currentQuestionIndex
) {}
