package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamMarkEntryBatchRequest {
    @NotNull
    private Long examId;
    @NotNull
    private List<ExamMarkEntryRequest> marks;
}
