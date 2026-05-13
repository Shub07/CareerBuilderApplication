package com.org.careerbuilder.dto.request;

import com.org.careerbuilder.models.enums.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherSessionAttendanceSubmitRequest {

    @NotBlank
    private String sessionKind;

    @NotNull
    private Long refId;

    @NotNull
    private LocalDate sessionDate;

    @NotEmpty
    private List<Row> rows;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Row {
        @NotNull
        private Long studentId;
        @NotNull
        private AttendanceStatus status;
        private String remarks;
    }
}
