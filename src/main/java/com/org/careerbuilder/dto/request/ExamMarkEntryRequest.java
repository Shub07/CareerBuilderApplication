package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotNull;
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
public class ExamMarkEntryRequest {
    @NotNull
    private Long resultId;
    /** When null, existing marks are left unchanged (remark-only update). */
    private Double marks;
    private String remark;
}
