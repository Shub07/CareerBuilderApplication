package com.org.careerbuilder.dto.response;

import com.org.careerbuilder.models.enums.AttendanceStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for attendance record response
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecordResponse {

    private Long attendanceId;

    private Long studentId;

    private String studentName;

    private String rollNo;

    private LocalDate date;

    private AttendanceStatus status;

    private String className;

    private String section;

    private Long subjectId;

    private String subjectName;

    private Long markedById;

    private String markedByName;

    private String remarks;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

