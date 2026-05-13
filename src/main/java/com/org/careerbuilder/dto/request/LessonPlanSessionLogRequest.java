package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class LessonPlanSessionLogRequest {

    @NotNull
    private LocalDate sessionDate;

    private LocalTime sessionTime;

    private String notes;

    private boolean markTopicCompleted;
}
