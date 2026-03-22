package com.org.careerbuilder.dto.response;

import java.util.List;

/**
 * Subject-wise performance block
 */
public record SubjectPerformanceResponse(

        String subject,
        String grade,
        int percentage,
        String scoreText,
        List<TestScoreResponse> tests

) {}