package com.org.careerbuilder.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 📌 Upcoming Exams Response
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class UpcomingExamResponse {
    
    @JsonProperty("exam_id")
    private Long examId;
    
    @JsonProperty("subject_name")
    private String subjectName;
    
    @JsonProperty("exam_name")
    private String examName;
    
    @JsonProperty("exam_date")
    private LocalDate examDate;
    
    @JsonProperty("start_time")
    private LocalTime startTime;
    
    @JsonProperty("duration_hours")
    private Integer durationHours;
    
    @JsonProperty("duration_text")
    private String durationText;  // "2 hours"
    
    @JsonProperty("topics")
    private String topics;  // "Laws of Motion, Energy"
    
    @JsonProperty("exam_type")
    private String examType;  // "Written", "Practical"
    
    @JsonProperty("days_remaining")
    private Long daysRemaining;
    
    @JsonProperty("is_today")
    private Boolean isToday;
    
    @JsonProperty("status")
    private String status;  // "In 3 days", "Tomorrow", "Today"
}

