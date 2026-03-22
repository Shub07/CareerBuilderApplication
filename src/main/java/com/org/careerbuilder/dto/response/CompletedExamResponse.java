package com.org.careerbuilder.dto.response;

public record CompletedExamResponse(
        Long examId,
        String subject,
        String examName,
        String score,
        String grade,
        String examDate
) {
}
