package com.org.careerbuilder.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTOs for Teacher portal "My Classes" (Invitto-style session hub).
 */
public final class TeacherMyClassesDtos {

    private TeacherMyClassesDtos() {
    }

    public record ClassSessionCard(
            String sessionKind,
            Long refId,
            LocalDate sessionDate,
            LocalTime startTime,
            LocalTime endTime,
            String timeRangeLabel,
            String gradeDisplay,
            String className,
            String section,
            Long subjectId,
            String subjectName,
            String lastTopic,
            String cardStatus,
            boolean attendancePending,
            boolean extraSession,
            String substituteForDisplay,
            String source,
            int studentCount
    ) {
    }

    public record ClassSessionListResponse(List<ClassSessionCard> sessions) {
    }

    public record SessionHeaderResponse(
            String sessionKind,
            Long refId,
            LocalDate sessionDate,
            LocalTime startTime,
            LocalTime endTime,
            String timeRangeLabel,
            String gradeDisplay,
            String className,
            String section,
            Long subjectId,
            String subjectName,
            String substituteForDisplay,
            boolean extraSession
    ) {
    }

    public record LessonNoteResponse(
            String topicCovered,
            String descriptionNotes,
            String homework
    ) {
    }

    public record AttendanceRosterRow(
            Long studentId,
            Integer rollNo,
            String displayName,
            String status
    ) {
    }

    public record AttendanceRosterResponse(
            LocalDate date,
            String className,
            String section,
            long presentCount,
            long absentCount,
            long lateCount,
            long leaveCount,
            long notMarkedCount,
            List<AttendanceRosterRow> students
    ) {
    }

    public record ImportAttendancePreviewRow(
            Integer rollNo,
            Long studentId,
            String displayName,
            String suggestedStatus,
            Double confidence
    ) {
    }

    public record ImportAttendancePreviewResponse(
            List<ImportAttendancePreviewRow> rows,
            String message
    ) {
    }

    public record AssignmentPublishedResponse(Long assignmentId, String title) {
    }

    public record ExtraSessionCreatedResponse(Long scheduleEntryId) {
    }

    public record AcademicYearsResponse(List<String> academicYears) {
    }

    public record SearchHitStudent(Long studentId, String displayName, Integer rollNo, String className, String section) {
    }

    public record SearchHitClass(String className, String section, String label) {
    }

    public record SearchHitNotice(Long noticeId, String title, String excerpt) {
    }

    public record TeacherPortalSearchResponse(
            List<SearchHitStudent> students,
            List<SearchHitClass> classes,
            List<SearchHitNotice> notices
    ) {
    }

    public record TeachingLogRow(
            LocalDate date,
            LocalTime startTime,
            String timeLabel,
            String gradeDisplay,
            String className,
            String section,
            String subjectName,
            String status,
            String takenAs
    ) {
    }

    public record TeachingLogTableResponse(List<TeachingLogRow> rows) {
    }
}
