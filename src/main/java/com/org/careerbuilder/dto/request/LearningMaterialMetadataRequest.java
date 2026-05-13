package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LearningMaterialMetadataRequest {

    @NotBlank
    @Size(max = 300)
    private String title;

    @NotBlank
    @Size(max = 50)
    private String className;

    @NotBlank
    @Size(max = 10)
    private String section;

    @NotNull
    private Long subjectId;

    private Long topicId;

    private LocalDate dueDate;

    private Boolean visibleToStudents;
}
