package com.org.careerbuilder.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassAttendanceReportResponse {
    private String className;
    private String section;
    private LocalDate reportDate;
    private int totalStudents;
    private int presentCount;
    private int absentCount;
    private int leaveCount;
    private double attendancePercentage;
    private LocalDate generatedAt;
}


