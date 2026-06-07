package com.org.careerbuilder.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class AdminStudentProfileDtos {

    private AdminStudentProfileDtos() {
    }

    public record ProfileHeader(
            Long studentId,
            String fullName,
            String email,
            String photoUrl,
            String registrationNumber,
            String status,
            String className,
            String section,
            Integer rollNumber,
            boolean flagged,
            String flagReason
    ) {
    }

    public record FullProfileResponse(
            ProfileHeader header,
            PersonalInfo personal,
            ContactInfo contact,
            PerformanceOverview performance,
            AttendanceOverview attendance,
            AcademicInfo academic,
            AdditionalInfo additional,
            List<DocumentRow> documents
    ) {
    }

    public record PersonalInfo(
            String fullName,
            String registrationNumber,
            Integer rollNumber,
            String gender,
            String dateOfBirth,
            String bloodGroup,
            String category,
            String religion,
            String nationality,
            String aadharNumber,
            String status,
            String admissionDate
    ) {
    }

    public record ContactInfo(
            String primaryContact,
            String fatherName,
            String motherName,
            String phone,
            String emergencyPhone,
            String email,
            String fullAddress
    ) {
    }

    public record PerformanceOverview(
            String averageGrade,
            String gradeLabel,
            double averageScorePercent,
            String rankLabel,
            List<SubjectScoreBar> subjectScores
    ) {
    }

    public record SubjectScoreBar(String subjectName, double percentScore) {
    }

    public record AttendanceOverview(
            double overallPercent,
            AttendanceBreakdown breakdown
    ) {
    }

    public record AttendanceBreakdown(
            double presentPercent,
            double latePercent,
            double absentPercent,
            double leavePercent
    ) {
    }

    public record AcademicInfo(
            String registrationNumber,
            Integer rollNumber,
            String className,
            String section,
            String academicYear,
            String admissionDate,
            String admissionType,
            String previousSchool
    ) {
    }

    public record AdditionalInfo(
            String transportRoute,
            String medicalConditions,
            String hostelStatus,
            String emergencyContact,
            String specialNotes
    ) {
    }

    public record DocumentRow(
            Long documentId,
            String documentName,
            String documentType,
            String uploadedBy,
            String uploadDate,
            String fileUrl
    ) {
    }

    public record PerformanceTabResponse(
            PerformanceOverview overview,
            List<ExamResultRow> recentExams
    ) {
    }

    public record ExamResultRow(
            Long examId,
            String examName,
            String examType,
            String subjectName,
            Integer obtainedMarks,
            Integer totalMarks,
            double percent,
            String grade,
            String examDate
    ) {
    }

    public record AttendanceTabResponse(
            AttendanceOverview overview,
            List<AttendanceDayRow> records
    ) {
    }

    public record AttendanceDayRow(String date, String status, String remarks) {
    }

    public record FeesTabResponse(
            String overallStatus,
            double totalPaid,
            double totalPending,
            List<FeeRow> fees
    ) {
    }

    public record FeeRow(
            Long feeId,
            String feeType,
            double amount,
            double paidAmount,
            String status,
            String dueDate,
            String academicYear
    ) {
    }

    public record CertificatesTabResponse(List<CertificateRow> certificates) {
    }

    public record CertificateRow(
            Long certificateId,
            String name,
            String category,
            String academicYear,
            String issuedDate
    ) {
    }

    public record ActivityLogTabResponse(List<ActivityLogRow> items) {
    }

    public record ActivityLogRow(
            Long logId,
            String activityType,
            String title,
            String description,
            String performedBy,
            LocalDateTime createdAt
    ) {
    }

    public record StudyActivityTabResponse(
            List<StudyActivityRow> activities,
            double completionRate
    ) {
    }

    public record StudyActivityRow(
            String date,
            String title,
            String activityType,
            boolean completed,
            String startTime,
            String endTime
    ) {
    }

    public record MessageSentResponse(Long messageId, String message, LocalDateTime sentAt) {
    }

    public record ActionResponse(String message) {
    }

    public record ProfileExportCatalogResponse(
            Map<String, List<String>> sections,
            List<String> datePeriods,
            List<String> formats
    ) {
    }

    // ─── Academic Performance tab (Quiz / Weekly / Internal / Midterm / Final / Assignments) ───

    public record AcademicPerformanceResponse(
            PerformanceOverview overview,
            String activeCategory,
            List<String> categories,
            List<PerformanceRecordRow> records
    ) {
    }

    public record PerformanceRecordRow(
            Long recordId,
            String name,
            String subjectName,
            String date,
            Integer obtainedMarks,
            Integer totalMarks,
            Double percent,
            String grade,
            Integer rank,
            String rankTrend,
            String status,
            Long assignmentId
    ) {
    }

    public record PerformanceExportCatalogResponse(
            List<String> categories,
            List<String> fields,
            List<String> formats
    ) {
    }

    public record AssignmentDetailResponse(
            Long assignmentId,
            String title,
            String subjectName,
            String studentName,
            String assignedDate,
            String deadline,
            String assignedBy,
            String instructions,
            List<SubmittedFileRow> submittedFiles,
            SubmissionDetail submission,
            List<ReferenceFileRow> referenceMaterials
    ) {
    }

    public record SubmittedFileRow(String fileName, String fileType, String uploadDate, String sizeLabel, String fileUrl) {
    }

    public record ReferenceFileRow(String fileName, String sizeLabel, String fileUrl) {
    }

    public record SubmissionDetail(
            String status,
            String submissionDate,
            String timingStatus,
            Integer obtainedMarks,
            Integer totalMarks,
            String grade,
            String teacherFeedback
    ) {
    }

    // ─── Daily Activity tab ─────────────────────────────────────────────────────

    public record DailyActivityTabResponse(
            TimeAllocationSummary allocation,
            List<ActivityMetricCard> metricCards,
            List<ActivityTrendDay> trend,
            List<DailyActivityLogRow> activityLog
    ) {
    }

    public record TimeAllocationSummary(
            double studyPercent,
            double schoolPercent,
            double physicalPercent,
            double restPercent
    ) {
    }

    public record ActivityMetricCard(String label, String durationLabel, String categoryKey) {
    }

    public record ActivityTrendDay(String dayLabel, double studyHours, double schoolHours, double physicalHours, double restHours) {
    }

    public record DailyActivityLogRow(String date, String activity, String time, String duration, String remarks) {
    }

    // ─── Attendance tab (subject-wise) ──────────────────────────────────────────

    public record AttendanceSummaryTabResponse(
            double overallPercent,
            int lowAttendanceSubjectCount,
            int totalAbsenceDays,
            List<SubjectAttendanceRow> subjects
    ) {
    }

    public record SubjectAttendanceRow(
            Long subjectId,
            String subjectName,
            int classesConducted,
            int classesAttended,
            double attendancePercent,
            String statusLabel,
            String standingLabel
    ) {
    }

    public record SubjectAttendanceDetailResponse(
            Long subjectId,
            String subjectName,
            String standingLabel,
            double attendancePercent,
            int presentCount,
            int absentCount,
            List<SubjectAttendanceHistoryRow> history
    ) {
    }

    public record SubjectAttendanceHistoryRow(
            String date,
            String day,
            String time,
            String status,
            String markedBy,
            String remarks
    ) {
    }

    // ─── Fees tab (enhanced) ────────────────────────────────────────────────────

    public record FeesTabDetailResponse(
            FeeSummaryCard summary,
            List<FeePaymentRow> paymentHistory
    ) {
    }

    public record FeeSummaryCard(
            double totalAnnualFees,
            double totalPaid,
            double totalPending,
            String nextDueDate,
            String overallStatus
    ) {
    }

    public record FeePaymentRow(
            Long feeId,
            String feeType,
            double amount,
            String dateOrDue,
            String status,
            String actionType,
            boolean receiptAvailable
    ) {
    }

    public record PaymentLinkResponse(String paymentUrl, String token, String expiresAt) {
    }

    public record FeeReminderResponse(String message, int feesNotified) {
    }
}
