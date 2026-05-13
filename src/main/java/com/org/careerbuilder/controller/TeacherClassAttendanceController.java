package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.ClassAttendanceSaveDraftRequest;
import com.org.careerbuilder.dto.request.ClassAttendanceSubmitRequest;
import com.org.careerbuilder.dto.response.TeacherClassAttendanceDtos;
import com.org.careerbuilder.service.TeacherClassAttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * Teacher portal: Class Attendance (mark roster, submit + lock, history, export, import preview).
 */
@RestController
@RequestMapping("/api/teacher/class-attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class TeacherClassAttendanceController {

    private final TeacherClassAttendanceService teacherClassAttendanceService;

    @GetMapping("/{facultyId}/filters")
    public ResponseEntity<TeacherClassAttendanceDtos.FiltersResponse> filters(@PathVariable Long facultyId) {
        return ResponseEntity.ok(teacherClassAttendanceService.getFilters(facultyId));
    }

    @GetMapping("/{facultyId}/roster")
    public ResponseEntity<TeacherClassAttendanceDtos.RosterResponse> roster(
            @PathVariable Long facultyId,
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam Long subjectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sessionDate) {
        return ResponseEntity.ok(teacherClassAttendanceService.getRoster(facultyId, className, section, subjectId, sessionDate));
    }

    @PutMapping("/{facultyId}/roster")
    public ResponseEntity<TeacherClassAttendanceDtos.RosterResponse> saveDraft(
            @PathVariable Long facultyId,
            @Valid @RequestBody ClassAttendanceSaveDraftRequest request) {
        return ResponseEntity.ok(teacherClassAttendanceService.saveDraft(facultyId, request));
    }

    @PostMapping(value = "/{facultyId}/submit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TeacherClassAttendanceDtos.SubmitResponse> submitJson(
            @PathVariable Long facultyId,
            @Valid @RequestBody ClassAttendanceSubmitRequest request) {
        return ResponseEntity.ok(teacherClassAttendanceService.submit(facultyId, request, null));
    }

    @PostMapping(value = "/{facultyId}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TeacherClassAttendanceDtos.SubmitResponse> submitMultipart(
            @PathVariable Long facultyId,
            @Valid @RequestPart("data") ClassAttendanceSubmitRequest request,
            @RequestPart(value = "proof", required = false) MultipartFile proof) {
        return ResponseEntity.ok(teacherClassAttendanceService.submit(facultyId, request, proof));
    }

    @PostMapping("/{facultyId}/mark-all-present")
    public ResponseEntity<TeacherClassAttendanceDtos.RosterResponse> markAllPresent(
            @PathVariable Long facultyId,
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam Long subjectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sessionDate) {
        return ResponseEntity.ok(teacherClassAttendanceService.markAllPresent(facultyId, className, section, subjectId, sessionDate));
    }

    @PostMapping("/{facultyId}/reset-marks")
    public ResponseEntity<Void> resetMarks(
            @PathVariable Long facultyId,
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam Long subjectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sessionDate) {
        teacherClassAttendanceService.resetMarks(facultyId, className, section, subjectId, sessionDate);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{facultyId}/import-preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TeacherClassAttendanceDtos.ImportPreviewResponse> importPreview(
            @PathVariable Long facultyId,
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam Long subjectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sessionDate,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(teacherClassAttendanceService.importPreview(
                facultyId, className, section, subjectId, sessionDate, file));
    }

    @GetMapping("/{facultyId}/history")
    public ResponseEntity<TeacherClassAttendanceDtos.HistoryPage> history(
            @PathVariable Long facultyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String quick,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        LocalDate[] range = resolveQuickRange(quick, from, to);
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        return ResponseEntity.ok(teacherClassAttendanceService.getHistory(
                facultyId, range[0], range[1], className, section, subjectId, pageable));
    }

    @GetMapping("/{facultyId}/history/export.csv")
    public ResponseEntity<Resource> exportHistory(
            @PathVariable Long facultyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String quick,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) Long subjectId) {
        LocalDate[] range = resolveQuickRange(quick, from, to);
        Resource resource = teacherClassAttendanceService.exportHistoryCsv(
                facultyId, range[0], range[1], className, section, subjectId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"class-attendance-history.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

    @GetMapping("/{facultyId}/sessions/{sessionId}")
    public ResponseEntity<TeacherClassAttendanceDtos.SessionDetailResponse> sessionDetail(
            @PathVariable Long facultyId,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(teacherClassAttendanceService.getSessionDetail(facultyId, sessionId));
    }

    private static LocalDate[] resolveQuickRange(String quick, LocalDate from, LocalDate to) {
        if (quick == null || quick.isBlank()) {
            LocalDate f = from != null ? from : LocalDate.now().minusMonths(3);
            LocalDate t = to != null ? to : LocalDate.now();
            return new LocalDate[]{f, t};
        }
        LocalDate n = LocalDate.now();
        return switch (quick.trim().toLowerCase()) {
            case "today" -> new LocalDate[]{n, n};
            case "tomorrow" -> new LocalDate[]{n.plusDays(1), n.plusDays(1)};
            case "week" -> {
                LocalDate mon = n.minusDays((n.getDayOfWeek().getValue() + 6) % 7);
                yield new LocalDate[]{mon, mon.plusDays(6)};
            }
            default -> new LocalDate[]{
                    from != null ? from : n.minusMonths(3),
                    to != null ? to : n
            };
        };
    }
}
