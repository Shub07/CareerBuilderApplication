package com.org.careerbuilder.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CareerReportResponse {

    private Long resultId;
    private Long assessmentId;
    private String assessmentTitle;
    private String assessmentType;
    private String bgColor;
    private Integer score;
    private boolean available;
    private String reportUrl;
    private LocalDateTime completedAt;
}
