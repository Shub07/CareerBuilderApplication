package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamMarksEditRequest {
    @NotNull
    private Long resultId;
    @NotNull
    private Double marks;
    private String remark;
    @NotBlank(message = "Reason for editing is mandatory")
    private String reasonForEdit;
}

