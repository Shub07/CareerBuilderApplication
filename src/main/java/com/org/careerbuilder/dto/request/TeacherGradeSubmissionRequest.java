package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherGradeSubmissionRequest {

    @NotNull
    private Integer pointsObtained;

    private String teacherRemarks;
}
