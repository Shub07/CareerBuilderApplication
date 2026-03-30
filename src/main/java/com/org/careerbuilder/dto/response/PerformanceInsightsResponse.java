package com.org.careerbuilder.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * 📌 Performance Insights Response
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class PerformanceInsightsResponse {
    
    @JsonProperty("class_average")
    private Integer classAverage;  // 78
    
    @JsonProperty("highest_score")
    private Integer highestScore;  // 98
    
    @JsonProperty("your_rank")
    private Integer yourRank;  // 5
    
    @JsonProperty("total_students")
    private Integer totalStudents;  // 30
    
    @JsonProperty("rank_text")
    private String rankText;  // "#5"
    
    @JsonProperty("performance_label")
    private String performanceLabel;  // "Excellent", "Good", "Average"
    
    @JsonProperty("comparison_with_average")
    private Integer comparisonWithAverage;  // +19 (your score - class average)
    
    @JsonProperty("is_above_average")
    private Boolean isAboveAverage;
}

