package com.org.careerbuilder.dto.response;

import java.util.List;

/**
 * Response payloads for the Admin Class Details module.
 */
public final class AdminClassDtos {

    private AdminClassDtos() {
    }

    // ─── List page ────────────────────────────────────────────────────────────

    public record ClassStats(
            long totalClasses,
            long totalSections,
            long totalStudents,
            long unassignedClasses
    ) {
    }

    public record ClassListResponse(
            ClassStats stats,
            List<ClassRow> rows,
            int page,
            int size,
            long totalElements,
            int totalPages,
            String showingLabel,
            String appliedStatsFilter,
            String appliedAcademicYear
    ) {
    }

    public record ClassRow(
            Long classId,
            String name,
            List<String> sections,
            String classTeacherName,
            String classTeacherPhotoUrl,
            long enrolled,
            Integer capacity,
            String status,
            boolean unassigned
    ) {
    }

    public record AcademicYearsResponse(
            List<String> academicYears,
            String currentAcademicYear
    ) {
    }

    public record CreateClassResponse(
            Long classId,
            String name,
            String academicYear,
            String message
    ) {
    }

    public record ActionResponse(boolean success, String message) {
    }

    public record DeletePreviewResponse(
            Long classId,
            String name,
            String warningMessage
    ) {
    }

    // ─── Detail header ──────────────────────────────────────────────────────────

    public record ClassDetailResponse(
            Long classId,
            String name,
            String academicYear,
            String gradeLevel,
            long totalSections,
            long totalStudents,
            String classTeacherName,
            String classTeacherPhotoUrl,
            String status,
            List<String> availableTabs
    ) {
    }

    // ─── Sections tab ─────────────────────────────────────────────────────────

    public record SectionsTabResponse(
            List<SectionRow> sections,
            long totalSections
    ) {
    }

    public record SectionRow(
            Long sectionId,
            String name,
            String classTeacherName,
            String classTeacherPhotoUrl,
            long totalStudents
    ) {
    }

    // ─── Subjects tab ─────────────────────────────────────────────────────────

    public record SubjectsTabResponse(
            List<SubjectRow> subjects,
            long totalSubjects
    ) {
    }

    public record SubjectRow(
            Long classSubjectId,
            Long subjectId,
            String subjectName,
            String subjectCode,
            Long assignedTeacherId,
            String assignedTeacherName
    ) {
    }

    // ─── Teachers tab ─────────────────────────────────────────────────────────

    public record TeachersTabResponse(
            List<TeacherAllocationRow> allocations,
            long totalTeachers
    ) {
    }

    public record TeacherAllocationRow(
            Long allocationId,
            Long teacherId,
            String teacherName,
            String role,
            String roleLabel,
            String subjectName,
            String sectionsLabel
    ) {
    }

    // ─── Students tab ─────────────────────────────────────────────────────────

    public record StudentsTabResponse(
            List<EnrolledStudentRow> students,
            int page,
            int size,
            long totalElements,
            int totalPages,
            String showingLabel
    ) {
    }

    public record EnrolledStudentRow(
            Long studentId,
            String name,
            String photoUrl,
            String regNumber,
            String gender,
            String section,
            String contact,
            String status
    ) {
    }

    /** Result for the "Search Student" dropdown in the Add Student modal. */
    public record StudentSearchResult(
            Long studentId,
            String name,
            String regNumber,
            String currentPlacement
    ) {
    }

    public record BulkPreviewResponse(
            List<BulkPreviewRow> rows,
            int validCount,
            int errorCount
    ) {
    }

    public record BulkPreviewRow(
            String name,
            String regNumber,
            String section,
            String status,
            String message
    ) {
    }

    public record BulkUploadResult(
            int addedCount,
            int skippedCount,
            String message
    ) {
    }

    // ─── Activity log tab ─────────────────────────────────────────────────────

    public record ActivityLogTabResponse(
            List<ActivityRow> entries,
            int page,
            int size,
            long totalElements,
            boolean hasMore
    ) {
    }

    public record ActivityRow(
            Long logId,
            String activity,
            String module,
            String date,
            String performedBy
    ) {
    }

    // ─── Form dropdown options (modals) ─────────────────────────────────────────

    public record FormOptionsResponse(
            List<TeacherOption> teachers,
            List<SubjectOption> subjects,
            List<SectionOption> sections
    ) {
    }

    public record TeacherOption(Long teacherId, String name, Long subjectId, String subjectName) {
    }

    public record SubjectOption(Long subjectId, String name) {
    }

    public record SectionOption(Long sectionId, String name) {
    }
}
