package com.org.careerbuilder.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * 📌 Question-wise Analysis Response
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class QuestionWiseAnalysisResponse {
    
    @JsonProperty("question_number")
    private Integer questionNumber;  // Q1, Q2, etc.
    
    @JsonProperty("question_text")
    private String questionText;
    
    @JsonProperty("status")
    private String status;  // "CORRECT", "WRONG", "PARTIAL"
    
    @JsonProperty("status_label")
    private String statusLabel;  // "Correct", "Wrong"
    
    @JsonProperty("marks_obtained")
    private Integer marksObtained;  // 5
    
    @JsonProperty("total_marks")
    private Integer totalMarks;  // 5
    
    @JsonProperty("section")
    private String section;  // "Section A: MCQ"
    
    @JsonProperty("is_correct")
    private Boolean isCorrect;
}

