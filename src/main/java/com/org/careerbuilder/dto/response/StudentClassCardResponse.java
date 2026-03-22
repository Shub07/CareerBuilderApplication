package com.org.careerbuilder.dto.response;

public record StudentClassCardResponse(
        Long subjectId,
        String subjectName,
        String topic,
        String teacherName,
        String timeText,
        Integer studentsEnrolled,
        Integer progressPercent,
        String actionText
) {}