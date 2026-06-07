package com.org.careerbuilder.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Response DTOs for {@code /api/teacher/student-performance}.
 */
public final class TeacherStudentPerformanceDtos {

    private TeacherStudentPerformanceDtos() {
    }

    public record ClassSectionFilter(String className, String section, String label) {
    }

    public record SubjectFilter(Long id, String name) {
    }

    public record ExamFilter(Long id, String name, LocalDate examDate) {
    }

    public record FiltersResponse(
            List<ClassSectionFilter> grades,
            List<SubjectFilter> subjects,
            List<ExamFilter> exams
    ) {
    }

    public record KpiResponse(
            double classAveragePercent,
            String classAverageTrendText,
            double passPercent,
            String passStatusLabel,
            long activeStudents,
            String activeStudentsSubtitle,
            double improvementRatePercent,
            String improvementStatusLabel
    ) {
    }

    public record StudentRowResponse(
            Long studentId,
            String displayName,
            String initials,
            Integer rollNo,
            Integer rank,
            Double attendancePercent,
            Double averageMarksPercent,
            Double improvementPercent
    ) {
    }

    public record SubjectBreakdownRow(
            Long subjectId,
            String subjectName,
            String letterGrade,
            Double attendancePercent,
            Double examAveragePercent
    ) {
    }

    public record TrendPoint(String label, LocalDate date, double scorePercent) {
    }

    public record StudentDetailResponse(
            Long studentId,
            String displayName,
            String initials,
            Integer rollNo,
            String className,
            String section,
            Double overallAveragePercent,
            Double overallAttendancePercent,
            List<SubjectBreakdownRow> subjects,
            List<TrendPoint> lastTestsTrend,
            String savedRemark
    ) {
    }

    public record OverviewCard(String title, String primaryValue, String secondaryText) {
    }

    public record ActivityItem(
            String title,
            String description,
            LocalDate activityDate,
            String severity
    ) {
    }

    public record OverviewResponse(
            List<OverviewCard> cards,
            List<ActivityItem> recentActivities
    ) {
    }

    public record ExamPerformanceRow(
            String examName,
            LocalDate examDate,
            String subjectName,
            String marksText,
            String letterGrade,
            String teacherRemark
    ) {
    }

    public record QuizPerformanceRow(
            String title,
            LocalDate quizDate,
            String subjectName,
            String scoreText,
            String status
    ) {
    }

    public record AssignmentPerformanceRow(
            String title,
            String subjectName,
            LocalDate dueDate,
            LocalDateTime submittedOn,
            String status,
            String grade
    ) {
    }

    public record AttendanceSummary(long present, long absent, long late) {
    }

    public record AttendanceDetailRow(
            LocalDate date,
            String status,
            LocalTime checkInTime
    ) {
    }

    public record EmailQueuedResponse(boolean queued, String message) {
    }
}
