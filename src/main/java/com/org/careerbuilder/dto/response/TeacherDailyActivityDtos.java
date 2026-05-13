package com.org.careerbuilder.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class TeacherDailyActivityDtos {
    private TeacherDailyActivityDtos() {}

    public record ClassSectionFilter(String className, String section, String label) {}

    public record StudentActivityRow(
            Long studentId,
            String studentName,
            Integer rollNo,
            String initials,
            double schoolHours,
            double studyHours,
            double physicalHours,
            double restHours,
            String status
    ) {}

    public record ListSummaryCards(
            double avgStudyHours,
            double avgPhysicalHours,
            double avgRestHours,
            int loggedTodayPercent
    ) {}

    public record DailyStudentListResponse(
            LocalDate date,
            String className,
            String section,
            ListSummaryCards summary,
            List<StudentActivityRow> rows
    ) {}

    public record StudentDetailResponse(
            Long studentId,
            String studentName,
            Integer rollNo,
            String className,
            String section,
            LocalDate date,
            double schoolHours,
            double studyHours,
            double physicalHours,
            double restHours,
            List<DaySegment> dayOverview,
            List<ActivityLogItem> activityLog,
            WeeklySummary weeklySummary,
            String status
    ) {}

    public record DaySegment(String label, LocalTime startTime, LocalTime endTime, double hours, String color) {}

    public record ActivityLogItem(String activityType, LocalTime startTime, LocalTime endTime, double hours, String note, String color) {}

    public record WeeklySummary(double dailyAverageStudy, double dailyAveragePhysical, double dailyAverageRest) {}

    public record ReviewResponse(Long reviewId, Long studentId, LocalDate date, String status, String note) {}
}
