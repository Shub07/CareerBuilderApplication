package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
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
public class DailyLifeActivityRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Activity date is required")
    private LocalDate activityDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotBlank(message = "Activity type is required")
    private String activityType;

    @Size(max = 500, message = "Note cannot exceed 500 characters")
    private String note;

    private Boolean completed;
}