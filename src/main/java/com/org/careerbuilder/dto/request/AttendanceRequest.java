package com.org.careerbuilder.dto.request;

import com.org.careerbuilder.models.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Status is required")
    private AttendanceStatus status;

    private String className;

    private String section;

    private Long subjectId;

    private Long markedById;

    private String remarks;
}

