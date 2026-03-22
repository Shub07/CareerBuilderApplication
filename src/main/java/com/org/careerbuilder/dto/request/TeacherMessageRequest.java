package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TeacherMessageRequest(
        @NotBlank String subject,
        @NotBlank String message
) {}