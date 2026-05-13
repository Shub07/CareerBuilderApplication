package com.org.careerbuilder.dto.request;

import com.org.careerbuilder.models.enums.TeacherDailyActivityStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherDailyActivityReviewRequest {
    @NotNull
    private Long studentId;
    private LocalDate date;
    @NotNull
    private TeacherDailyActivityStatus status;
    private String note;
}
