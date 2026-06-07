package com.org.careerbuilder.dto.response;

import com.org.careerbuilder.dto.response.AdminTeacherDtos.ClassAssignmentRow;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class AdminTeacherProfileDtos {

    private AdminTeacherProfileDtos() {
    }

    public record FullProfileResponse(
            ProfileHeader header,
            BasicDetailsTab basicDetails,
            List<String> availableTabs
    ) {
    }

    public record ProfileHeader(
            Long facultyId,
            String fullName,
            String employeeId,
            String specializationLabel,
            String designation,
            String status,
            String photoUrl,
            String phone,
            String email,
            LocalDate joiningDate,
            String roleLabel
    ) {
    }

    public record BasicDetailsTab(
            PersonalInfo personal,
            ProfessionalInfo professional,
            List<DocumentRow> documents
    ) {
    }

    public record PersonalInfo(
            String fullName,
            String gender,
            LocalDate dateOfBirth,
            String phone,
            String email,
            String address
    ) {
    }

    public record ProfessionalInfo(
            String qualification,
            String experienceLabel,
            String specialization,
            String employmentType,
            LocalDate joiningDate,
            String status,
            String department
    ) {
    }

    public record DocumentRow(
            Long documentId,
            String documentName,
            String documentType,
            String fileName,
            String uploadDate,
            long fileSizeBytes
    ) {
    }

    public record AllocationsTabResponse(
            List<ClassAssignmentRow> assignments,
            int totalClasses
    ) {
    }

    public record AssignmentsTabResponse(
            List<AssignmentRow> assignments,
            int totalCount
    ) {
    }

    public record AssignmentRow(
            Long assignmentId,
            String title,
            String classLabel,
            String subjectName,
            LocalDate assignedDate,
            int submissionRatePercent,
            String status
    ) {
    }

    /** "View Assignment" detail screen. */
    public record AssignmentDetailResponse(
            Long assignmentId,
            String title,
            String classLabel,
            String subjectName,
            LocalDate assignedDate,
            LocalDate deadline,
            String description,
            Integer totalMarks,
            SubmissionSummary summary,
            List<StudentSubmissionRow> students
    ) {
    }

    public record SubmissionSummary(
            int submitted,
            int pending,
            int graded,
            int totalStudents,
            int submissionRatePercent
    ) {
    }

    public record StudentSubmissionRow(
            Long submissionId,
            Long studentId,
            String studentName,
            LocalDate submissionDate,
            String status,
            String marksLabel
    ) {
    }

    public record AttendanceLeaveTabResponse(
            AttendanceSummary attendance,
            List<RecentAttendanceRow> recentAttendance,
            List<LeaveRow> leaveHistory
    ) {
    }

    public record AttendanceSummary(
            int presentDays,
            int absentDays,
            int leaveBalance,
            double attendancePercent
    ) {
    }

    public record RecentAttendanceRow(
            LocalDate date,
            String status,
            String remarks
    ) {
    }

    public record LeaveRow(
            Long leaveId,
            String leaveType,
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate appliedOn,
            String reason,
            String status,
            boolean reviewable
    ) {
    }

    public record WorkloadTabResponse(
            int weeklyClasses,
            int subjectsAssigned,
            int sectionsAssigned,
            List<ScheduleRow> weeklyTimetable
    ) {
    }

    public record ScheduleRow(
            String dayLabel,
            String period,
            String classLabel,
            String subjectName
    ) {
    }

    public record EmploymentTabResponse(
            String employeeId,
            LocalDate joiningDate,
            String employmentType,
            String department,
            String status,
            String designation,
            String basicSalaryLabel,
            String contractType,
            List<EmploymentDocumentRow> documents
    ) {
    }

    public record EmploymentDocumentRow(
            String label,
            String downloadUrl,
            boolean available
    ) {
    }

    public record ActivityLogTabResponse(
            List<ActivityLogRow> entries,
            int page,
            int size,
            long totalElements,
            boolean hasMore
    ) {
    }

    public record ActivityLogRow(
            Long logId,
            String activity,
            String module,
            String date,
            String performedBy
    ) {
    }

    public record ProfileExportCatalogResponse(
            Map<String, List<String>> fieldsByCategory,
            Map<String, String> fieldLabels
    ) {
    }

    public record MoreActionsMenuResponse(
            List<MenuAction> actions
    ) {
    }

    public record MenuAction(String code, String label, boolean destructive) {
    }

    public record DocumentTypeOption(String code, String label) {
    }
}
