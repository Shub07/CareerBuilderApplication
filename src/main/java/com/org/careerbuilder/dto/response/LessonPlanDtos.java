package com.org.careerbuilder.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class LessonPlanDtos {

    private LessonPlanDtos() {
    }

    public record ClassSectionOption(String className, String section, String label) {
    }

    public record SubjectOption(Long subjectId, String subjectName) {
    }

    public record LessonPlanFiltersResponse(List<ClassSectionOption> classSections, List<SubjectOption> subjects) {
    }

    public record TopicRow(
            Long topicId,
            int displayOrder,
            String title,
            String description,
            String status,
            LocalDate lastTaughtDate,
            int sessionsCount
    ) {
    }

    public record TopicDetailResponse(
            Long topicId,
            String title,
            String description,
            int sortOrder,
            String status,
            LocalDate lastTaughtDate,
            int sessionsCount,
            String className,
            String section,
            String classLabel,
            Long subjectId,
            String subjectName
    ) {
    }

    public record SessionLogRow(
            Long logId,
            LocalDate sessionDate,
            String sessionTimeDisplay,
            String notes,
            boolean hasMaterial,
            String materialFileNameHint,
            boolean markTopicCompleted
    ) {
    }

    public record LearningMaterialRow(
            Long materialId,
            String title,
            String originalFilename,
            Long fileSizeBytes,
            String mimeType,
            LocalDateTime uploadedAt,
            String classLabel,
            Long subjectId,
            String subjectName,
            Long topicId,
            String topicTitle,
            boolean visibleToStudents,
            LocalDate dueDate
    ) {
    }

    /** Student-facing topic (same class + subject as logged-in student). */
    public record StudentTopicRow(
            Long topicId,
            int displayOrder,
            String title,
            String description,
            String status,
            LocalDate lastTaughtDate,
            int sessionsCount,
            String subjectName,
            String teacherName
    ) {
    }

    public record StudentMaterialRow(
            Long materialId,
            String title,
            String originalFilename,
            Long fileSizeBytes,
            String mimeType,
            LocalDateTime uploadedAt,
            String subjectName,
            String topicTitle,
            LocalDate dueDate
    ) {
    }
}
