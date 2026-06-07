package com.org.careerbuilder.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class TeacherScheduleDtos {

    private TeacherScheduleDtos() {
    }

    /**
     * Unified row: school class timetable slot and/or teacher-created activity.
     */
    public record ScheduleItemResponse(
            String source,
            Long sourceId,
            String title,
            String activityType,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String timeRangeLabel,
            String sectionLabel,
            String venue,
            boolean showViewClass,
            String className,
            String section,
            String status,
            String notes,
            /** Headcount for className+section in this school, when applicable. */
            Integer studentCount
    ) {
    }

    public record DayScheduleResponse(LocalDate date, String dayName, List<ScheduleItemResponse> items) {
    }

    public record WeekScheduleResponse(List<DayScheduleResponse> days) {
    }

    public record ClassSectionOption(
            String className,
            String section,
            String label
    ) {
    }

    public record ScheduleFiltersResponse(List<ClassSectionOption> classes) {
    }

    public record ScheduleEntryResponse(
            Long id,
            String title,
            String activityType,
            boolean recurring,
            Integer dayOfWeek,
            LocalDate specificDate,
            LocalTime startTime,
            LocalTime endTime,
            String className,
            String section,
            String sectionLabel,
            String venue,
            String notes,
            Long subjectId,
            String substituteForName
    ) {
    }
}
