package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.TeacherScheduleEntryRequest;
import com.org.careerbuilder.dto.response.TeacherScheduleDtos;
import com.org.careerbuilder.service.TeacherScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/teacher/schedule")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class TeacherScheduleController {

    private final TeacherScheduleService teacherScheduleService;

    @GetMapping("/{facultyId}/filters")
    public ResponseEntity<TeacherScheduleDtos.ScheduleFiltersResponse> filters(@PathVariable Long facultyId) {
        return ResponseEntity.ok(teacherScheduleService.getScheduleFilters(facultyId));
    }

    /**
     * Daily view: merged school class timetable slots for classes this teacher teaches + teacher-created activities.
     */
    @GetMapping("/{facultyId}/day")
    public ResponseEntity<TeacherScheduleDtos.DayScheduleResponse> day(
            @PathVariable Long facultyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section) {
        LocalDate d = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(teacherScheduleService.getDaySchedule(facultyId, d, className, section));
    }

    /**
     * Weekly view: seven days starting Monday {@code weekStart} (defaults to Monday of current week).
     */
    @GetMapping("/{facultyId}/week")
    public ResponseEntity<TeacherScheduleDtos.WeekScheduleResponse> week(
            @PathVariable Long facultyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section) {
        LocalDate monday = weekStart;
        if (monday == null) {
            LocalDate today = LocalDate.now();
            monday = today.minusDays((today.getDayOfWeek().getValue() + 6) % 7);
        }
        return ResponseEntity.ok(teacherScheduleService.getWeekSchedule(facultyId, monday, className, section));
    }

    @PostMapping("/{facultyId}/entries")
    public ResponseEntity<TeacherScheduleDtos.ScheduleEntryResponse> create(
            @PathVariable Long facultyId,
            @Valid @RequestBody TeacherScheduleEntryRequest request) {
        return ResponseEntity.ok(teacherScheduleService.createEntry(facultyId, request));
    }

    @PutMapping("/{facultyId}/entries/{entryId}")
    public ResponseEntity<TeacherScheduleDtos.ScheduleEntryResponse> update(
            @PathVariable Long facultyId,
            @PathVariable Long entryId,
            @Valid @RequestBody TeacherScheduleEntryRequest request) {
        return ResponseEntity.ok(teacherScheduleService.updateEntry(facultyId, entryId, request));
    }

    @DeleteMapping("/{facultyId}/entries/{entryId}")
    public ResponseEntity<Void> delete(@PathVariable Long facultyId, @PathVariable Long entryId) {
        teacherScheduleService.deleteEntry(facultyId, entryId);
        return ResponseEntity.noContent().build();
    }
}
