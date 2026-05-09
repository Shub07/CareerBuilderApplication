package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

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

