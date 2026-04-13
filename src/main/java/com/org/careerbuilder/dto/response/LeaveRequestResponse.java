package com.org.careerbuilder.dto.response;

import com.org.careerbuilder.models.enums.LeaveStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

/**
 * DTO for leave request response
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestResponse {

    private Long id;

    private Long studentId;

    private String studentName;

    private String leaveType;

    private LocalDate fromDate;

    private LocalDate toDate;

    private String reason;

    private LeaveStatus status;

    private Boolean isHalfDay;

    private String halfDayPeriod;

    private Integer durationDays;

    private String className;

    private String section;

    private LocalDateTime appliedOn;

    private LocalDateTime approvedOn;

    private String approvedBy;

    private String rejectionReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
