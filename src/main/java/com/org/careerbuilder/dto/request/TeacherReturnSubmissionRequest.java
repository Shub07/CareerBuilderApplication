package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherReturnSubmissionRequest {

    @NotBlank
    private String teacherRemarks;
}
