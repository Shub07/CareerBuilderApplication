package com.org.careerbuilder.dto.request;

import com.org.careerbuilder.models.enums.AssignmentPublishStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherAssignmentUpdateRequest {

    private String title;
    private String description;
    private Long subjectId;
    private String className;
    private String section;
    private LocalDate dueDate;
    private LocalTime dueTime;
    private Integer totalMarks;
    private Boolean allowLateSubmission;
    private Boolean allowResubmission;
    private AssignmentPublishStatus publishStatus;
    private LocalDate givenDate;
}
