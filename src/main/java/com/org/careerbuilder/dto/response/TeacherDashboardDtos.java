package com.org.careerbuilder.dto.response;

import java.time.Instant;
import java.util.List;

public final class TeacherDashboardDtos {

    private TeacherDashboardDtos() {
    }

    /**
     * @param kind SUBMISSION | NOTICE (used by UI for icon/colour)
     */
    public record DashboardPendingActionRow(
            String id,
            String title,
            Instant occurredAt,
            String kind
    ) {
    }

    public record PerformanceSnapshot(
            String classLabel,
            String className,
            String section,
            double classAveragePercent,
            String classAverageTrendText,
            double passPercent,
            String passStatusLabel,
            int activeStudents,
            double improvementRatePercent,
            String improvementStatusLabel
    ) {
    }

    /** Today&apos;s class sessions from My Classes (timetable + extra), for dashboard and portal. */
    public record TodayClassSessionRow(
            String sessionKind,
            Long refId,
            String sessionDate,
            String subjectName,
            String gradeDisplay,
            String className,
            String section,
            String timeRangeLabel,
            int studentCount,
            boolean attendancePending,
            String cardStatus,
            String lastTopic
    ) {
    }

    public record TeacherDashboardResponse(
            String greeting,
            String summaryLine,
            int teachingPeriodsToday,
            List<DashboardPendingActionRow> pendingActions,
            PerformanceSnapshot performanceSnapshot,
            List<TodayClassSessionRow> todayClassSessions,
            int attendancePendingToday
    ) {
    }
}
