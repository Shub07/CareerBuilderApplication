package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.*;
import com.org.careerbuilder.dto.response.TeacherAssignmentDtos;
import com.org.careerbuilder.service.TeacherAssignmentService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/assignments")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class TeacherAssignmentController {

    private final TeacherAssignmentService teacherAssignmentService;

    @GetMapping("/{facultyId}/filters")
    public ResponseEntity<TeacherAssignmentDtos.AssignmentFiltersResponse> filters(@PathVariable Long facultyId) {
        return ResponseEntity.ok(teacherAssignmentService.getFilters(facultyId));
    }

    @GetMapping("/{facultyId}")
    public ResponseEntity<TeacherAssignmentDtos.AssignmentListPage> list(
            @PathVariable Long facultyId,
            @RequestParam(defaultValue = "current") String view,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueTo,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String displayStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        return ResponseEntity.ok(teacherAssignmentService.listAssignments(
                facultyId, view, className, section, subjectId, dueFrom, dueTo, month, search, displayStatus, pageable));
    }

    @GetMapping("/{facultyId}/{assignmentId}")
    public ResponseEntity<TeacherAssignmentDtos.AssignmentDetailResponse> detail(
            @PathVariable Long facultyId,
            @PathVariable Long assignmentId) {
        return ResponseEntity.ok(teacherAssignmentService.getDetail(facultyId, assignmentId));
    }

    @GetMapping("/{facultyId}/{assignmentId}/submissions")
    public ResponseEntity<TeacherAssignmentDtos.SubmissionsPage> submissions(
            @PathVariable Long facultyId,
            @PathVariable Long assignmentId,
            @RequestParam(required = false, defaultValue = "ALL") String statusTab,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(200, Math.max(1, size)));
        return ResponseEntity.ok(teacherAssignmentService.listSubmissions(facultyId, assignmentId, statusTab, pageable));
    }

    @GetMapping("/{facultyId}/{assignmentId}/submissions/{submissionId}/review")
    public ResponseEntity<TeacherAssignmentDtos.SubmissionReviewResponse> review(
            @PathVariable Long facultyId,
            @PathVariable Long assignmentId,
            @PathVariable Long submissionId) {
        return ResponseEntity.ok(teacherAssignmentService.getSubmissionReview(facultyId, assignmentId, submissionId));
    }

    @PostMapping(value = "/{facultyId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Long>> createJson(
            @PathVariable Long facultyId,
            @Valid @RequestBody TeacherAssignmentCreateRequest request) {
        Long id = teacherAssignmentService.createAssignment(facultyId, request, null);
        return ResponseEntity.status(201).body(Map.of("assignmentId", id));
    }

    @PostMapping(value = "/{facultyId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Long>> createMultipart(
            @PathVariable Long facultyId,
            @Valid @RequestPart("data") TeacherAssignmentCreateRequest request,
            @RequestPart(value = "attachment", required = false) MultipartFile attachment) {
        Long id = teacherAssignmentService.createAssignment(facultyId, request, attachment);
        return ResponseEntity.status(201).body(Map.of("assignmentId", id));
    }

    @PutMapping(value = "/{facultyId}/{assignmentId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateJson(
            @PathVariable Long facultyId,
            @PathVariable Long assignmentId,
            @Valid @RequestBody TeacherAssignmentUpdateRequest request) {
        teacherAssignmentService.updateAssignment(facultyId, assignmentId, request, null);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{facultyId}/{assignmentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateMultipart(
            @PathVariable Long facultyId,
            @PathVariable Long assignmentId,
            @Valid @RequestPart("data") TeacherAssignmentUpdateRequest request,
            @RequestPart(value = "attachment", required = false) MultipartFile attachment) {
        teacherAssignmentService.updateAssignment(facultyId, assignmentId, request, attachment);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{facultyId}/{assignmentId}")
    public ResponseEntity<Void> delete(@PathVariable Long facultyId, @PathVariable Long assignmentId) {
        teacherAssignmentService.deleteAssignment(facultyId, assignmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{facultyId}/{assignmentId}/duplicate")
    public ResponseEntity<TeacherAssignmentDtos.DuplicateAssignmentResponse> duplicate(
            @PathVariable Long facultyId,
            @PathVariable Long assignmentId) {
        return ResponseEntity.ok(teacherAssignmentService.duplicateAssignment(facultyId, assignmentId));
    }

    @PutMapping("/{facultyId}/{assignmentId}/submissions/{submissionId}/grade")
    public ResponseEntity<Void> grade(
            @PathVariable Long facultyId,
            @PathVariable Long assignmentId,
            @PathVariable Long submissionId,
            @Valid @RequestBody TeacherGradeSubmissionRequest request) {
        teacherAssignmentService.gradeSubmission(facultyId, assignmentId, submissionId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{facultyId}/{assignmentId}/submissions/{submissionId}/return")
    public ResponseEntity<Void> returnToStudent(
            @PathVariable Long facultyId,
            @PathVariable Long assignmentId,
            @PathVariable Long submissionId,
            @Valid @RequestBody TeacherReturnSubmissionRequest request) {
        teacherAssignmentService.returnSubmission(facultyId, assignmentId, submissionId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{facultyId}/{assignmentId}/students/{studentId}/submission-file")
    public ResponseEntity<Resource> downloadSubmission(
            @PathVariable Long facultyId,
            @PathVariable Long assignmentId,
            @PathVariable Long studentId) {
        Resource resource = teacherAssignmentService.downloadSubmissionFile(facultyId, assignmentId, studentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .body(resource);
    }

    @GetMapping("/{facultyId}/{assignmentId}/export-submissions.zip")
    public ResponseEntity<Resource> exportZip(@PathVariable Long facultyId, @PathVariable Long assignmentId) {
        Resource resource = teacherAssignmentService.exportAllSubmissionsZip(facultyId, assignmentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"assignment-submissions.zip\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(resource);
    }
}
