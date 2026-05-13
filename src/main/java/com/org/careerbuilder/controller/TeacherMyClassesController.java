package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.*;
import com.org.careerbuilder.dto.response.TeacherMyClassesDtos;
import com.org.careerbuilder.service.TeacherMyClassesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/teacher/classes")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class TeacherMyClassesController {

    private final TeacherMyClassesService teacherMyClassesService;

    /**
     * My Classes grid: sessions for today / tomorrow / week / month / history, optional academic year and text search.
     */
    @GetMapping("/{facultyId}")
    public ResponseEntity<TeacherMyClassesDtos.ClassSessionListResponse> listSessions(
            @PathVariable Long facultyId,
            @RequestParam(defaultValue = "today") String timeRange,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(teacherMyClassesService.listSessions(facultyId, timeRange, academicYear, search));
    }

    @GetMapping("/{facultyId}/academic-years")
    public ResponseEntity<TeacherMyClassesDtos.AcademicYearsResponse> academicYears(@PathVariable Long facultyId) {
        return ResponseEntity.ok(teacherMyClassesService.listAcademicYears());
    }

    @PostMapping("/{facultyId}/extra-sessions")
    public ResponseEntity<TeacherMyClassesDtos.ExtraSessionCreatedResponse> createExtraSession(
            @PathVariable Long facultyId,
            @Valid @RequestBody TeacherExtraSessionRequest request) {
        return ResponseEntity.ok(teacherMyClassesService.createExtraSession(facultyId, request));
    }

    @GetMapping("/{facultyId}/session")
    public ResponseEntity<TeacherMyClassesDtos.SessionHeaderResponse> sessionHeader(
            @PathVariable Long facultyId,
            @RequestParam String sessionKind,
            @RequestParam Long refId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sessionDate) {
        return ResponseEntity.ok(teacherMyClassesService.getSessionHeader(facultyId, sessionKind, refId, sessionDate));
    }

    @GetMapping("/{facultyId}/session/lesson-notes")
    public ResponseEntity<TeacherMyClassesDtos.LessonNoteResponse> getLessonNotes(
            @PathVariable Long facultyId,
            @RequestParam String sessionKind,
            @RequestParam Long refId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sessionDate) {
        return ResponseEntity.ok(teacherMyClassesService.getLessonNotes(facultyId, sessionKind, refId, sessionDate));
    }

    @PutMapping("/{facultyId}/session/lesson-notes")
    public ResponseEntity<TeacherMyClassesDtos.LessonNoteResponse> saveLessonNotes(
            @PathVariable Long facultyId,
            @Valid @RequestBody TeacherLessonNoteRequest request) {
        return ResponseEntity.ok(teacherMyClassesService.saveLessonNotes(facultyId, request));
    }

    @GetMapping("/{facultyId}/session/attendance-roster")
    public ResponseEntity<TeacherMyClassesDtos.AttendanceRosterResponse> attendanceRoster(
            @PathVariable Long facultyId,
            @RequestParam String sessionKind,
            @RequestParam Long refId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sessionDate) {
        return ResponseEntity.ok(teacherMyClassesService.getAttendanceRoster(facultyId, sessionKind, refId, sessionDate));
    }

    @PostMapping("/{facultyId}/session/attendance")
    public ResponseEntity<Void> submitAttendance(
            @PathVariable Long facultyId,
            @Valid @RequestBody TeacherSessionAttendanceSubmitRequest request) {
        teacherMyClassesService.submitSessionAttendance(facultyId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{facultyId}/session/attendance/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TeacherMyClassesDtos.ImportAttendancePreviewResponse> importAttendancePreview(
            @PathVariable Long facultyId,
            @RequestParam String sessionKind,
            @RequestParam Long refId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sessionDate,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(teacherMyClassesService.previewAttendanceImport(facultyId, sessionKind, refId, sessionDate, file));
    }

    @PostMapping(value = "/{facultyId}/session/assignments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TeacherMyClassesDtos.AssignmentPublishedResponse> publishAssignment(
            @PathVariable Long facultyId,
            @RequestParam String sessionKind,
            @RequestParam Long refId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sessionDate,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @RequestPart(value = "attachment", required = false) MultipartFile attachment) {
        return ResponseEntity.ok(teacherMyClassesService.publishSessionAssignment(
                facultyId, sessionKind, refId, sessionDate, title, description, dueDate, attachment));
    }

    @GetMapping("/{facultyId}/teaching-log/export.csv")
    public ResponseEntity<Resource> exportTeachingLog(
            @PathVariable Long facultyId,
            @RequestParam(required = false) String academicYear) {
        Resource resource = teacherMyClassesService.exportTeachingLog(facultyId, academicYear);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"teaching-log.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }
}
