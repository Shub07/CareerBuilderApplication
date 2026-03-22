package com.org.careerbuilder.dto.response;

/**
 * Individual test/exam score
 */
public record TestScoreResponse(

        String examType,
        String date,
        String score,
        int percentage

) {}