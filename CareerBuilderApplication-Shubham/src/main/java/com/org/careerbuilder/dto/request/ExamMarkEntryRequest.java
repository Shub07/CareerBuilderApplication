package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamMarkEntryRequest {
    @NotNull
    private Long studentId;
    @NotNull
    private Double marks;
    private String remark;
}

