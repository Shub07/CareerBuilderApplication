package com.org.careerbuilder.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CareerPerformanceTrendPointResponse {

    private String label;
    private int score;
}
