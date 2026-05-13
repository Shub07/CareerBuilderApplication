package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.TeacherDailyActivityReviewRequest;
import com.org.careerbuilder.dto.response.TeacherDailyActivityDtos;
import com.org.careerbuilder.service.TeacherDailyActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/teacher/daily-activity")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class TeacherDailyActivityController {

    private final TeacherDailyActivityService service;

    @GetMapping("/{facultyId}/filters")
    public ResponseEntity<List<TeacherDailyActivityDtos.ClassSectionFilter>> filters(@PathVariable Long facultyId) {
        return ResponseEntity.ok(service.getFilters(facultyId));
    }

    @GetMapping("/{facultyId}/students")
    public ResponseEntity<TeacherDailyActivityDtos.DailyStudentListResponse> studentList(
            @PathVariable Long facultyId,
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(service.getStudentList(facultyId, className, section, range, date, search));
    }

    @GetMapping("/{facultyId}/students/{studentId}")
    public ResponseEntity<TeacherDailyActivityDtos.StudentDetailResponse> studentDetail(
            @PathVariable Long facultyId,
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.getStudentDetail(facultyId, studentId, date));
    }

    @PutMapping("/{facultyId}/review")
    public ResponseEntity<TeacherDailyActivityDtos.ReviewResponse> review(
            @PathVariable Long facultyId,
            @Valid @RequestBody TeacherDailyActivityReviewRequest request) {
        return ResponseEntity.ok(service.upsertReview(facultyId, request));
    }
}
