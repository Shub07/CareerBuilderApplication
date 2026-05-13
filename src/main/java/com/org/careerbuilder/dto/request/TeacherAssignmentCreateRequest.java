package com.org.careerbuilder.dto.request;

import com.org.careerbuilder.models.enums.AssignmentPublishStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherAssignmentCreateRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Long subjectId;

    @NotBlank
    private String className;

    @NotBlank
    private String section;

    @NotNull
    private LocalDate dueDate;

    private LocalTime dueTime;

    private Integer totalMarks;

    private Boolean allowLateSubmission;

    private Boolean allowResubmission;

    /** DRAFT or PUBLISHED (default PUBLISHED). */
    private AssignmentPublishStatus publishStatus;

    private LocalDate givenDate;
}
