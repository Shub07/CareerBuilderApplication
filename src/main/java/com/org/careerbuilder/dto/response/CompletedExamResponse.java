package com.org.careerbuilder.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * 📌 Completed Exam Response (List view)
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class CompletedExamResponse {
    
    @JsonProperty("exam_id")
    private Long examId;
    
    @JsonProperty("subject_name")
    private String subjectName;
    
    @JsonProperty("exam_name")
    private String examName;
    
    @JsonProperty("score")
    private String score;  // "97/100"
    
    @JsonProperty("percentage")
    private Integer percentage;  // 97
    
    @JsonProperty("grade")
    private String grade;  // "Grade A"
    
    @JsonProperty("exam_date")
    private String examDate;  // "Nov 5, 2025"
    
    @JsonProperty("subject_icon")
    private String subjectIcon;  // Icon color/type
}
