package com.org.careerbuilder.dto.response;

/**
 * DTO for Teacher Message Response after successfully sending a message
 */
public record TeacherMessageResponse(
        Long messageId,
        String subject,
        String message,
        String sentAt,
        String status
) {}

