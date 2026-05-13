package com.org.careerbuilder.dto.request;

import com.org.careerbuilder.models.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassAttendanceLineWrite {

    @NotNull
    private Long studentId;

    @NotNull
    private AttendanceStatus status;

    private String remarks;
}
