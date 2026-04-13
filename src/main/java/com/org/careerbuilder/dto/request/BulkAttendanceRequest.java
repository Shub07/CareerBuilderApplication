package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for bulk attendance marking
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkAttendanceRequest {

    @NotNull(message = "School ID is required")
    private Long schoolId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Class name is required")
    private String className;

    @NotNull(message = "Section is required")
    private String section;

    private Long subjectId;

    @NotEmpty(message = "Attendance records cannot be empty")
    private List<AttendanceEntry> records;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttendanceEntry {
        @NotNull(message = "Student ID is required")
        private Long studentId;

        @NotNull(message = "Status is required")
        private String status; // PRESENT, ABSENT, LEAVE

        private String remarks;
    }
}

