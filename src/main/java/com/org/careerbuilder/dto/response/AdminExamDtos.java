package com.org.careerbuilder.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class AdminExamDtos {

    private AdminExamDtos() {
    }

    public record ExamStatsResponse(
            long totalExams,
            long activeExams,
            long upcomingExams,
            long completedExams
    ) {
    }

    public record ExamListResponse(
            List<ExamRow> exams,
            int page,
            int size,
            long totalElements,
            int totalPages,
            String showingLabel
    ) {
    }

    public record ExamRow(
            Long examId,
            String examName,
            String examType,
            String examTypeLabel,
            String classLabel,
            LocalDate startDate,
            LocalDate endDate,
            String status,
            String statusLabel,
            boolean canEdit,
            boolean canDelete
    ) {
    }

    public record ExamFilterOptionsResponse(
            List<String> classes,
            List<ExamOption> examTypes,
            List<ExamOption> statuses,
            List<String> academicYears
    ) {
    }

    public record ExamOption(String value, String label) {
    }

    public record ExamDetailResponse(
            Long examId,
            String examName,
            String examType,
            String examTypeLabel,
            String classLabel,
            String section,
            String academicYear,
            LocalDate startDate,
            LocalDate endDate,
            String description,
            String instructions,
            Long coordinatorId,
            String coordinatorName,
            String status,
            String statusLabel,
            long scheduleCount,
            boolean resultsPublished,
            LocalDateTime resultsPublishedAt,
            long assignedStudentCount
    ) {
    }

    public record TimetableResponse(
            Long examId,
            String examName,
            List<ScheduleRow> schedules
    ) {
    }

    public record ScheduleRow(
            Long scheduleId,
            Long subjectId,
            String subjectName,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String timeSlotLabel,
            String durationLabel,
            String venue,
            Long venueId,
            Long invigilatorId,
            String invigilatorName,
            Long assistantInvigilatorId,
            String assistantInvigilatorName,
            Integer durationMinutes,
            LocalTime reportingTime,
            Integer bufferMinutes,
            String instructions
    ) {
    }

    public record VenueRow(Long venueId, String name, int capacity) {
    }

    public record VenueAvailabilityResponse(
            Long venueId,
            String name,
            int capacity,
            int allocated,
            int available
    ) {
    }

    public record SectionOption(String section, long studentCount) {
    }

    public record AssignmentPreviewResponse(
            long totalSelected,
            List<AssignmentPreviewRow> students,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }

    public record AssignmentPreviewRow(
            Long studentId,
            String studentName,
            String section,
            String rollNo
    ) {
    }

    public record AssignedStudentsResponse(
            List<AssignedStudentRow> students,
            int page,
            int size,
            long totalElements,
            int totalPages,
            String showingLabel
    ) {
    }

    public record AssignedStudentRow(
            Long registrationId,
            Long studentId,
            String studentName,
            String section,
            String hallTicketNumber,
            String primaryVenue,
            String status,
            String statusLabel
    ) {
    }

    public record BulkAssignResponse(
            boolean success,
            String message,
            int assignedCount,
            int skippedCount
    ) {
    }

    public record PublishResultsResponse(
            boolean success,
            String message,
            boolean resultsPublished,
            int notificationsQueued
    ) {
    }

    public record ActivityLogResponse(
            List<ActivityLogRow> entries,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }

    public record ActivityLogRow(
            Long id,
            String activityType,
            String title,
            String description,
            String performedBy,
            String createdAt
    ) {
    }

    public record ExportReportResponse(
            String fileName,
            String contentType,
            byte[] content
    ) {
    }

    public record ActionResponse(boolean success, String message) {
    }

    // ─── Hall tickets ───────────────────────────────────────────────────────────

    public record HallTicketListResponse(
            List<HallTicketRow> tickets,
            int page,
            int size,
            long totalElements,
            int totalPages,
            String showingLabel
    ) {
    }

    public record HallTicketRow(
            Long registrationId,
            Long studentId,
            String studentName,
            String hallTicketNumber,
            String assignedVenue,
            boolean hallTicketGenerated,
            boolean canView,
            boolean canDownload,
            boolean canPrint
    ) {
    }

    public record HallTicketGeneratePreviewResponse(
            long ticketCount,
            String rangeStart,
            String rangeEnd,
            String previewLabel
    ) {
    }

    public record HallTicketGenerateResponse(
            boolean success,
            String message,
            int generatedCount,
            String rangeStart,
            String rangeEnd,
            int notificationsQueued,
            String batchPdfFileName
    ) {
    }

    public record HallTicketPreviewResponse(
            String schoolName,
            String examTitle,
            String studentName,
            String hallTicketNumber,
            String admissionId,
            String classSection,
            String documentHash,
            boolean verified,
            List<HallTicketScheduleLine> schedule,
            List<String> instructions
    ) {
    }

    public record HallTicketScheduleLine(
            String subject,
            String date,
            String timing,
            String venue
    ) {
    }

    // ─── Venue allocation ───────────────────────────────────────────────────────

    public record VenueAllocationSummaryResponse(
            int totalVenues,
            long totalStudents,
            int availableSeats,
            List<VenueAllocationRow> venues
    ) {
    }

    public record VenueAllocationRow(
            Long allocationId,
            Long venueId,
            String venueName,
            int totalCapacity,
            long studentsAssigned,
            int availableSeats,
            Long administratorId,
            String administratorName,
            Long primaryInvigilatorId,
            String primaryInvigilatorName,
            Long assistantInvigilatorId,
            String assistantInvigilatorName
    ) {
    }

    // ─── Marks monitoring ───────────────────────────────────────────────────────

    public record MarksMonitoringResponse(List<MarksMonitoringRow> subjects) {
    }

    public record MarksMonitoringRow(
            Long subjectId,
            String subjectName,
            Long teacherId,
            String teacherName,
            String submissionStatus,
            String submissionStatusLabel,
            long marksEntered,
            long totalStudents,
            String progressLabel,
            boolean canView,
            boolean canApprove
    ) {
    }

    public record SubjectMarksDetailResponse(
            Long examId,
            String examName,
            Long subjectId,
            String subjectName,
            String classSection,
            int maxMarks,
            Long teacherId,
            String teacherName,
            String submissionStatus,
            String submissionStatusLabel,
            double classAverage,
            long passCount,
            long failCount,
            long totalStudents,
            List<MarksAuditEvent> auditTrail,
            List<StudentMarkRow> students
    ) {
    }

    public record MarksAuditEvent(String title, String timestamp, boolean completed) {
    }

    public record StudentMarkRow(
            Long resultId,
            Long studentId,
            String studentName,
            String rollId,
            Integer obtainedMarks,
            int totalMarks,
            String marksLabel,
            String grade,
            String verificationStatus,
            String verificationStatusLabel,
            boolean failing
    ) {
    }

    public record ApproveMarksResponse(boolean success, String message, String submissionStatus) {
    }

    // ─── Final results ──────────────────────────────────────────────────────────

    public record ExamResultsSummaryResponse(
            double overallPassPercent,
            double overallFailPercent,
            double averageScore,
            boolean resultsPublished,
            long totalStudents
    ) {
    }

    public record ExamResultsListResponse(
            List<FinalResultRow> results,
            int page,
            int size,
            long totalElements,
            int totalPages,
            String showingLabel
    ) {
    }

    public record FinalResultRow(
            Long studentId,
            String studentName,
            int totalObtained,
            int totalMax,
            String totalMarksLabel,
            String grade,
            int rank,
            String status,
            String statusLabel
    ) {
    }

    public record VenueAllocationDetailResponse(
            Long allocationId,
            Long venueId,
            String venueName,
            int venueBaseCapacity,
            Integer capacityLimit,
            long studentsAssigned,
            int availableSeats,
            Long administratorId,
            Long primaryInvigilatorId,
            Long assistantInvigilatorId
    ) {
    }
}
