package com.org.careerbuilder.dto.response;

public record ExamDetailResponse(
        String subject,
        String examName,
        String score,
        String grade,
        String date,
        Integer rank,
        String feedback
) {
}
