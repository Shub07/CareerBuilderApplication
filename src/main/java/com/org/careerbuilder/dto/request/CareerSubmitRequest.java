package com.org.careerbuilder.dto.request;

import java.util.Map;

/**
 * Final submission with all question answers.
 * answers: map of questionOrder (0-based) -> selectedOptionIndex
 */
public record CareerSubmitRequest(
        Map<Integer, Integer> answers
) {}
