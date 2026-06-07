package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.TeacherScheduleEntryRequest;
import com.org.careerbuilder.dto.response.TeacherScheduleDtos;

import java.time.LocalDate;

public interface TeacherScheduleService {

    TeacherScheduleDtos.ScheduleFiltersResponse getScheduleFilters(Long facultyId);

    TeacherScheduleDtos.DayScheduleResponse getDaySchedule(Long facultyId, LocalDate date, String className, String section);

    TeacherScheduleDtos.WeekScheduleResponse getWeekSchedule(Long facultyId, LocalDate weekStartMonday, String className, String section);

    TeacherScheduleDtos.ScheduleEntryResponse createEntry(Long facultyId, TeacherScheduleEntryRequest request);

    TeacherScheduleDtos.ScheduleEntryResponse updateEntry(Long facultyId, Long entryId, TeacherScheduleEntryRequest request);

    void deleteEntry(Long facultyId, Long entryId);
}
