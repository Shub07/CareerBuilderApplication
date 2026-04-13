package com.org.careerbuilder.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CareerResultResponse {

    private Long resultId;
    private Long assessmentId;
    private String assessmentTitle;
    private String assessmentType;
    private LocalDateTime completedAt;
    private List<String> strengthAreas;
    private List<String> interestIndicators;
    private Integer score;
    private boolean reportAvailable;
}
