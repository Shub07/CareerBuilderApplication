package com.org.careerbuilder.dto.response;

public record StudentCalendarSlotResponse(
        String timeRange,
        String subjectName,
        String title,
        String slotType
) {}