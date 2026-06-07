package com.org.careerbuilder.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class AdminTeacherDtos {

    private AdminTeacherDtos() {
    }

    public record TeacherStats(
            long totalTeachers,
            long activeTeachers,
            long onLeaveTeachers,
            long unassignedTeachers
    ) {
    }

    public record TeacherFilterOptions(
            List<SubjectOption> subjects,
            List<String> statuses,
            List<String> statsCardFilters,
            List<ClassSectionOption> classSections
    ) {
    }

    public record AcademicYearsResponse(
            List<String> academicYears,
            String currentAcademicYear
    ) {
    }

    public record SubjectOption(Long subjectId, String name) {
    }

    public record ClassSectionOption(String className, List<String> sections) {
    }

    public record TeacherSectionResponse(
            TeacherStats stats,
            List<TeacherRow> rows,
            int page,
            int size,
            long totalElements,
            int totalPages,
            String appliedStatsFilter,
            String appliedStatus,
            Long appliedSubjectId,
            String showingLabel
    ) {
    }

    public record TeacherRow(
            Long facultyId,
            String employeeId,
            String fullName,
            String email,
            String photoUrl,
            String specialization,
            Long subjectId,
            List<String> classAssignments,
            String status,
            boolean unassigned
    ) {
    }

    /** Lightweight header for View Profile action. */
    public record TeacherProfileSummaryResponse(
            Long facultyId,
            String employeeId,
            String fullName,
            String email,
            String phone,
            String photoUrl,
            String specialization,
            String status,
            List<String> classAssignments,
            LocalDate joiningDate
    ) {
    }

    /** Edit Teacher modal — pre-filled form + dropdown options. */
    public record EditFormResponse(
            TeacherDetailResponse teacher,
            List<String> genders,
            List<String> employmentTypes,
            List<String> departments,
            List<SubjectOption> subjects,
            List<ClassSectionOption> availableClassSections
    ) {
    }

    /** Delete confirmation modal copy. */
    public record DeletePreviewResponse(
            Long facultyId,
            String fullName,
            String employeeId,
            String warningMessage
    ) {
    }

    public record DeleteTeacherResponse(
            boolean success,
            String message,
            String fullName,
            Long facultyId
    ) {
    }

    /** Allocate Classes modal — class checkboxes + subject dropdown. */
    public record AllocateClassesFormResponse(
            Long facultyId,
            String teacherName,
            Long primarySubjectId,
            String primarySubjectName,
            List<SubjectOption> subjects,
            List<ClassSectionCheckbox> classSections,
            List<ClassAssignmentRow> currentAssignments
    ) {
    }

    public record ClassSectionCheckbox(
            String className,
            String section,
            String label,
            boolean selected
    ) {
    }

    public record AllocateClassesResponse(
            int createdCount,
            int skippedCount,
            int deactivatedCount,
            String message,
            List<ClassAssignmentRow> assignments
    ) {
    }

    public record TeacherDetailResponse(
            Long facultyId,
            String employeeId,
            String firstName,
            String lastName,
            String fullName,
            String gender,
            LocalDate dateOfBirth,
            Integer age,
            String phone,
            String email,
            String address,
            String photoUrl,
            String qualification,
            String specialization,
            Long subjectId,
            Integer experienceYears,
            String skills,
            String certifications,
            LocalDate joiningDate,
            String employmentType,
            String department,
            String status,
            String resumeUrl,
            String idProofUrl,
            String certificatesUrl,
            String experienceLettersUrl,
            List<ClassAssignmentRow> assignments
    ) {
    }

    public record ClassAssignmentRow(
            Long assignmentId,
            String className,
            String section,
            Long subjectId,
            String subjectName,
            String role,
            String roleLabel,
            LocalDate assignedDate,
            boolean active
    ) {
    }

    public record TeacherCreatedResponse(
            Long facultyId,
            String employeeId,
            String fullName,
            String email,
            String message
    ) {
    }

    public record ActionResponse(boolean success, String message) {
    }

    public record AssignmentCreatedResponse(Long assignmentId, String message) {
    }

    public record ExportFieldCatalogResponse(
            Map<String, List<String>> fieldsByCategory,
            Map<String, String> fieldLabels
    ) {
    }

    /** Preview for export modal subtitle ("6 teachers"). */
    public record ExportPreviewResponse(
            int teacherCount,
            String scope,
            List<String> suggestedFields
    ) {
    }
}
