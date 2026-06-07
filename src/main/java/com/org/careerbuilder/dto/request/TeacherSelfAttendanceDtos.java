package com.org.careerbuilder.dto.request;

import com.org.careerbuilder.models.enums.TeacherLeaveType;
import com.org.careerbuilder.models.enums.TeacherLeaveStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class TeacherSelfAttendanceDtos {
    private TeacherSelfAttendanceDtos() {}

    public record CheckInRequest(String lateNote) {}

    public record CheckOutRequest(String workSummary) {}

    public record LeaveApplyRequest(
            @NotNull TeacherLeaveType leaveType,
            @NotNull LocalDate fromDate,
            @NotNull LocalDate toDate,
            @NotBlank String reason,
            Long substituteFacultyId
    ) {}

    public record LeaveActionRequest(
            @NotNull TeacherLeaveStatus status,
            String adminName,
            String rejectionReason
    ) {}

    public record DashboardResponse(
            TodayStatusCard today,
            List<WeekRow> weekRows,
            LeaveBalanceCard leaveBalance
    ) {}

    public record TodayStatusCard(
            String label,
            String status,
            String checkInTime,
            String checkOutTime,
            String workingHours
    ) {}

    public record WeekRow(
            LocalDate date,
            String dayLabel,
            String checkInTime,
            String checkOutTime,
            String workingHours,
            String status
    ) {}

    /** Full attendance log row for history screen and export. */
    public record AttendanceHistoryRow(
            LocalDate date,
            String dateLabel,
            String checkInTime,
            String checkOutTime,
            String workingHours,
            String status
    ) {}

    public record LeaveBalanceCard(
            Bucket casual,
            Bucket medical,
            Bucket halfDay
    ) {}

    public record LeaveHistoryItem(
            Long leaveId,
            String leaveType,
            LocalDate fromDate,
            LocalDate toDate,
            int totalDays,
            String reason,
            String rejectionReason,
            String status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            boolean hasDocument,
            String documentFileName,
            String documentContentType,
            Long documentSizeBytes,
            String substituteName
    ) {}

    public record SubstituteTeacherOption(Long facultyId, String displayName) {}

    public record AdminLeaveItem(
            Long leaveId,
            Long facultyId,
            String facultyName,
            String leaveType,
            LocalDate fromDate,
            LocalDate toDate,
            String reason,
            String rejectionReason,
            String status,
            LocalDateTime createdAt
    ) {}

    public record Bucket(int remaining, int total) {}
}
