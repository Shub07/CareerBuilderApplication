package com.org.careerbuilder.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class TeacherAssignmentDtos {

    private TeacherAssignmentDtos() {
    }

    public record ClassSectionOption(String className, String section, String label) {
    }

    public record SubjectOption(Long subjectId, String subjectName) {
    }

    public record AssignmentFiltersResponse(
            List<ClassSectionOption> classSections,
            List<SubjectOption> subjects
    ) {
    }

    public record AssignmentListRow(
            Long assignmentId,
            String title,
            String subjectName,
            String classLabel,
            LocalDate dueDate,
            LocalTime dueTime,
            int submittedCount,
            int totalStudents,
            String displayStatus,
            String publishStatus
    ) {
    }

    public record AssignmentListPage(List<AssignmentListRow> content, int totalPages, long totalElements) {
    }

    public record AssignmentDetailResponse(
            Long assignmentId,
            String title,
            String description,
            String subjectName,
            Long subjectId,
            String className,
            String section,
            String classLabel,
            LocalDate dueDate,
            LocalTime dueTime,
            String publishStatus,
            String displayStatus,
            Integer totalMarks,
            boolean allowLateSubmission,
            boolean allowResubmission,
            LocalDate givenDate,
            String attachmentPath,
            int totalStudents,
            int submittedCount,
            int pendingCount,
            int lateCount,
            int checkedCount
    ) {
    }

    public record SubmissionRow(
            Long submissionId,
            Long studentId,
            String displayName,
            Integer rollNo,
            String initials,
            LocalDateTime submittedAt,
            String status,
            String marksDisplay,
            Integer pointsObtained,
            Integer pointsTotal
    ) {
    }

    public record SubmissionsPage(List<SubmissionRow> content, int totalPages, long totalElements) {
    }

    public record SubmissionReviewResponse(
            Long submissionId,
            Long assignmentId,
            Long studentId,
            String studentName,
            LocalDateTime submittedAt,
            String filePath,
            String originalFileNameHint,
            Integer pointsTotal,
            Integer pointsObtained,
            String teacherRemarks,
            String status
    ) {
    }

    public record DuplicateAssignmentResponse(Long newAssignmentId) {
    }
}
