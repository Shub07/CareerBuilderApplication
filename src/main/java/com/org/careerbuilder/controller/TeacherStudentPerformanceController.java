package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.TeacherStudentRemarkRequest;
import com.org.careerbuilder.dto.response.TeacherStudentPerformanceDtos;
import com.org.careerbuilder.service.TeacherStudentPerformanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/teacher/student-performance")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class TeacherStudentPerformanceController {

    private final TeacherStudentPerformanceService service;

    @GetMapping("/{facultyId}/filters")
    public ResponseEntity<TeacherStudentPerformanceDtos.FiltersResponse> filters(
            @PathVariable Long facultyId,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section) {
        return ResponseEntity.ok(service.getFilters(facultyId, className, section));
    }

    @GetMapping("/{facultyId}/class/kpis")
    public ResponseEntity<TeacherStudentPerformanceDtos.KpiResponse> classKpis(
            @PathVariable Long facultyId,
            @RequestParam String className,
            @RequestParam String section) {
        return ResponseEntity.ok(service.getClassKpis(facultyId, className, section));
    }

    @GetMapping("/{facultyId}/class/students")
    public ResponseEntity<List<TeacherStudentPerformanceDtos.StudentRowResponse>> studentRows(
            @PathVariable Long facultyId,
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long examId) {
        return ResponseEntity.ok(service.getStudentRows(facultyId, className, section, subjectId, examId));
    }

    @GetMapping("/{facultyId}/students/{studentId}/detail")
    public ResponseEntity<TeacherStudentPerformanceDtos.StudentDetailResponse> studentDetail(
            @PathVariable Long facultyId,
            @PathVariable Long studentId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long examId) {
        return ResponseEntity.ok(service.getStudentDetail(facultyId, studentId, subjectId, examId));
    }

    @GetMapping("/{facultyId}/students/{studentId}/overview")
    public ResponseEntity<TeacherStudentPerformanceDtos.OverviewResponse> overview(
            @PathVariable Long facultyId,
            @PathVariable Long studentId) {
        return ResponseEntity.ok(service.getStudentOverview(facultyId, studentId));
    }

    @GetMapping("/{facultyId}/students/{studentId}/exams")
    public ResponseEntity<List<TeacherStudentPerformanceDtos.ExamPerformanceRow>> exams(
            @PathVariable Long facultyId,
            @PathVariable Long studentId) {
        return ResponseEntity.ok(service.getStudentExams(facultyId, studentId));
    }

    @GetMapping("/{facultyId}/students/{studentId}/quizzes")
    public ResponseEntity<List<TeacherStudentPerformanceDtos.QuizPerformanceRow>> quizzes(
            @PathVariable Long facultyId,
            @PathVariable Long studentId) {
        return ResponseEntity.ok(service.getStudentQuizzes(facultyId, studentId));
    }

    @GetMapping("/{facultyId}/students/{studentId}/assignments")
    public ResponseEntity<List<TeacherStudentPerformanceDtos.AssignmentPerformanceRow>> assignments(
            @PathVariable Long facultyId,
            @PathVariable Long studentId) {
        return ResponseEntity.ok(service.getStudentAssignments(facultyId, studentId));
    }

    @GetMapping("/{facultyId}/students/{studentId}/attendance/summary")
    public ResponseEntity<TeacherStudentPerformanceDtos.AttendanceSummary> attendanceSummary(
            @PathVariable Long facultyId,
            @PathVariable Long studentId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return ResponseEntity.ok(service.getStudentAttendanceSummary(facultyId, studentId, from, to));
    }

    @GetMapping("/{facultyId}/students/{studentId}/attendance/details")
    public ResponseEntity<Page<TeacherStudentPerformanceDtos.AttendanceDetailRow>> attendanceDetails(
            @PathVariable Long facultyId,
            @PathVariable Long studentId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            Pageable pageable) {
        return ResponseEntity.ok(service.getStudentAttendanceDetails(facultyId, studentId, from, to, pageable));
    }

    @PutMapping("/{facultyId}/students/{studentId}/remarks")
    public ResponseEntity<Void> saveRemark(
            @PathVariable Long facultyId,
            @PathVariable Long studentId,
            @Valid @RequestBody TeacherStudentRemarkRequest request) {
        service.saveRemark(facultyId, studentId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{facultyId}/students/{studentId}/email-parents")
    public ResponseEntity<TeacherStudentPerformanceDtos.EmailQueuedResponse> emailParents(
            @PathVariable Long facultyId,
            @PathVariable Long studentId) {
        return ResponseEntity.ok(service.queueEmailToParents(facultyId, studentId));
    }

    @GetMapping("/{facultyId}/students/{studentId}/report.pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable Long facultyId,
            @PathVariable Long studentId) {
        byte[] pdf = service.buildPerformancePdf(facultyId, studentId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("student_" + studentId + "_performance.pdf")
                .build());
        headers.setContentType(MediaType.APPLICATION_PDF);
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
