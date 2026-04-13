package com.org.careerbuilder.dto.response;

import java.time.LocalDate;

public record AssignmentCardResponse(
        Long id,
        String title,
        String subject,
        String teacher,
        LocalDate dueDate,
        String status,
        String description
) {}