package com.org.careerbuilder.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class TeacherQuizDtos {

    private TeacherQuizDtos() {
    }

    public record ClassSectionOption(String className, String section, String label) {
    }

    public record SubjectOption(Long subjectId, String subjectName) {
    }

    public record QuizFiltersResponse(List<ClassSectionOption> classSections, List<SubjectOption> subjects) {
    }

    public record QuizCardRow(
            Long quizId,
            String title,
            String subjectName,
            String classLabel,
            String lifecycleStatus,
            int questionCount,
            int timeLimitMinutes,
            int attemptsCount,
            int totalStudents,
            Double averageScoreObtained,
            Integer totalMarks
    ) {
    }

    public record QuizListPage(List<QuizCardRow> content, int totalPages, long totalElements) {
    }

    public record QuizOptionResponse(Long optionId, int sortOrder, String optionText, boolean correct) {
    }

    public record QuizQuestionResponse(
            Long questionId,
            int sortOrder,
            String questionType,
            String questionText,
            BigDecimal maxMarks,
            String answerKey,
            List<QuizOptionResponse> options
    ) {
    }

    public record QuizDetailResponse(
            Long quizId,
            String title,
            String instructions,
            String className,
            String section,
            String classLabel,
            Long subjectId,
            String subjectName,
            Integer timeLimitMinutes,
            Integer totalMarks,
            LocalDateTime scheduledAt,
            boolean shuffleQuestions,
            String lifecycleStatus,
            LocalDate conductedOn,
            List<QuizQuestionResponse> questions
    ) {
    }

    public record QuizResultsSummary(
            int studentsAttempted,
            int totalStudents,
            int pendingReviewCount,
            BigDecimal averageScoreObtained,
            int totalMarks
    ) {
    }

    public record QuizResultStudentRow(
            Long submissionId,
            Long studentId,
            String displayName,
            Integer rollNo,
            String initials,
            BigDecimal scoreObtained,
            Integer totalMarks,
            BigDecimal percentage,
            String gradeStatus
    ) {
    }

    public record QuizResultsPage(
            QuizResultsSummary summary,
            List<QuizResultStudentRow> content,
            int totalPages,
            long totalElements
    ) {
    }

    public record QuizSubmissionReviewResponse(
            Long submissionId,
            Long quizId,
            Long studentId,
            String studentName,
            BigDecimal currentScore,
            Integer totalMarks,
            String teacherRemark,
            String gradeStatus,
            List<QuizAnswerReviewLine> lines
    ) {
    }

    public record QuizAnswerReviewLine(
            Long questionId,
            int sortOrder,
            String questionType,
            String questionText,
            BigDecimal maxMarks,
            String studentAnswerText,
            Long selectedOptionId,
            String selectedOptionText,
            BigDecimal marksAwarded
    ) {
    }

    public record DuplicateQuizResponse(Long newQuizId) {
    }
}
