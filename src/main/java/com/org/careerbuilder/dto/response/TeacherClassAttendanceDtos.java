package com.org.careerbuilder.dto.response;

import java.time.LocalDate;
import java.util.List;

public final class TeacherClassAttendanceDtos {

    private TeacherClassAttendanceDtos() {
    }

    public record ClassSectionOption(String className, String section, String label) {
    }

    public record SubjectOption(Long subjectId, String subjectName) {
    }

    public record FiltersResponse(
            List<ClassSectionOption> classSections,
            List<SubjectOption> subjects
    ) {
    }

    public record RosterStudentRow(
            Long studentId,
            Integer rollNo,
            String displayName,
            String initials,
            String status
    ) {
    }

    public record RosterResponse(
            Long sessionId,
            LocalDate sessionDate,
            String className,
            String section,
            Long subjectId,
            String subjectName,
            boolean locked,
            long presentCount,
            long absentCount,
            long lateCount,
            long leaveCount,
            long notMarkedCount,
            List<RosterStudentRow> students
    ) {
    }

    public record ImportPreviewRow(
            Integer rollNo,
            Long studentId,
            String displayName,
            String suggestedStatus,
            Double confidence
    ) {
    }

    public record ImportPreviewResponse(List<ImportPreviewRow> rows, String message) {
    }

    public record SubmitResponse(
            Long sessionId,
            boolean locked,
            int presentCount,
            int absentCount,
            int lateCount,
            int leaveCount,
            int notMarkedCount,
            int totalStudents
    ) {
    }

    public record HistoryRow(
            Long sessionId,
            LocalDate date,
            String gradeDisplay,
            String className,
            String section,
            String subjectName,
            int presentCount,
            int totalStudents,
            String takenByShort,
            boolean hasProof,
            boolean locked
    ) {
    }

    public record HistoryPage(List<HistoryRow> content, int totalPages, long totalElements) {
    }

    public record SessionDetailResponse(
            Long sessionId,
            LocalDate sessionDate,
            String className,
            String section,
            Long subjectId,
            String subjectName,
            boolean locked,
            String proofAttachmentPath,
            List<RosterStudentRow> students,
            List<EditAuditItem> recentEdits
    ) {
    }

    public record EditAuditItem(String reasonText, String editedAtIso, String facultyName) {
    }
}
