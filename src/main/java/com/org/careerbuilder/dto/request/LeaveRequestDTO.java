package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.*;

/**
 * DTO for creating/updating leave requests
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestDTO {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Leave type is required")
    @Size(min = 1, max = 50)
    private String leaveType;

    @NotNull(message = "From date is required")
    private LocalDate fromDate;

    @NotNull(message = "To date is required")
    private LocalDate toDate;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    private Boolean isHalfDay;

    private String halfDayPeriod; // "morning" or "afternoon"
}
