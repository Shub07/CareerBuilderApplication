package com.org.careerbuilder.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * 📌 Teacher Feedback Response
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class TeacherFeedbackResponse {
    
    @JsonProperty("teacher_name")
    private String teacherName;  // "Mrs. Lucia"
    
    @JsonProperty("teacher_id")
    private Long teacherId;
    
    @JsonProperty("feedback_text")
    private String feedbackText;  // "Excellent performance. Focus more on proof-based questions in the future."
    
    @JsonProperty("feedback_date")
    private String feedbackDate;
    
    @JsonProperty("is_available")
    private Boolean isAvailable;
}

