package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO for requesting attendance exceptions (sick leave, medical, field trip, etc.)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceExceptionRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Exception date is required")
    private LocalDate exceptionDate;

    @NotNull(message = "Exception type is required")
    private String exceptionType; // SICK_LEAVE, MEDICAL, FIELD_TRIP, etc.

    @NotNull(message = "Reason is required")
    private String reason;

    private String attachmentUrl; // For medical certificates, etc.
}

