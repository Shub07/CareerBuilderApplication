package com.org.careerbuilder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceSummaryResponse {
    private Long studentId;
    private String studentName;
    private String className;
    private String section;
    private int attendancePercentage;
    private long totalDaysExpected;
    private long totalDaysPresent;
    private long totalDaysAbsent;
    private long totalDaysLeave;
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private String status;
    private LocalDateTime lastUpdated;
}

