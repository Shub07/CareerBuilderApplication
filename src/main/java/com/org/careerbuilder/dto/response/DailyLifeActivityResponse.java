package com.org.careerbuilder.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyLifeActivityResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private LocalDate activityDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String activityType;
    private String activityTypeLabel;
    private String color;
    private String note;
    private boolean completed;
    private Integer durationMinutes;
    private String durationText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}