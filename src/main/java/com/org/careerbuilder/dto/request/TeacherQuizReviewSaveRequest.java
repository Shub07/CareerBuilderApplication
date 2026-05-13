package com.org.careerbuilder.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record TeacherQuizReviewSaveRequest(
        @Valid List<AnswerMark> answers,
        String teacherRemark
) {
    public record AnswerMark(
            @NotNull Long questionId,
            BigDecimal marksAwarded
    ) {
    }
}
