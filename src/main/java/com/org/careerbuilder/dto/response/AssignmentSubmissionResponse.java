package com.org.careerbuilder.dto.response;

import java.time.LocalDateTime;

public record AssignmentSubmissionResponse(
        Long id,
        Long assignmentId,
        String filePath,
        String comments,
        LocalDateTime submittedAt,
        String status
) {}