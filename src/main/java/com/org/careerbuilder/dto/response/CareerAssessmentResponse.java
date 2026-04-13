package com.org.careerbuilder.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CareerAssessmentResponse {

    private Long id;
    private String title;
    private String description;
    private String type;
    private String category;
    private String bgColor;
    private Integer estimatedMinutes;
    private Integer totalQuestions;
    private String status;         // PENDING / IN_PROGRESS / COMPLETED
    private int progressPercent;   // 0-100
    private Long progressId;       // null if PENDING
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private boolean hasResult;
}
