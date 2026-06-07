package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.AdminClassRequests;
import com.org.careerbuilder.dto.response.AdminClassDtos;
import com.org.careerbuilder.service.AdminClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Admin Class Details portal — classes, sections, subjects, teacher allocations.
 * Base path: /api/admin/classes
 */
@RestController
@RequestMapping("/api/admin/classes")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH,
                RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AdminClassController {

    private final AdminClassService classService;

    // ─── List page ────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<AdminClassDtos.ClassStats> stats(@RequestParam Long schoolId) {
        return ResponseEntity.ok(classService.getStats(schoolId));
    }

    @GetMapping("/academic-years")
    public ResponseEntity<AdminClassDtos.AcademicYearsResponse> academicYears(@RequestParam Long schoolId) {
        return ResponseEntity.ok(classService.getAcademicYears(schoolId));
    }

    @GetMapping
    public ResponseEntity<AdminClassDtos.ClassListResponse> list(
            @RequestParam Long schoolId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String statsFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(classService.listClasses(schoolId, q, academicYear, statsFilter, page, size));
    }

    @PostMapping
    public ResponseEntity<AdminClassDtos.CreateClassResponse> create(
            @Valid @RequestBody AdminClassRequests.CreateClassRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classService.createClass(request));
    }

    @PutMapping("/{classId}")
    public ResponseEntity<AdminClassDtos.ActionResponse> update(
            @PathVariable Long classId,
            @Valid @RequestBody AdminClassRequests.UpdateClassRequest request) {
        return ResponseEntity.ok(classService.updateClass(classId, request));
    }

    @GetMapping("/{classId}/delete-preview")
    public ResponseEntity<AdminClassDtos.DeletePreviewResponse> deletePreview(
            @RequestParam Long schoolId,
            @PathVariable Long classId) {
        return ResponseEntity.ok(classService.getDeletePreview(schoolId, classId));
    }

    @DeleteMapping("/{classId}")
    public ResponseEntity<AdminClassDtos.ActionResponse> delete(
            @RequestParam Long schoolId,
            @PathVariable Long classId,
            @RequestParam(required = false) String performedBy) {
        return ResponseEntity.ok(classService.deleteClass(schoolId, classId, performedBy));
    }

    // ─── Detail header + form options ────────────────────────────────────────────

    @GetMapping("/{classId}")
    public ResponseEntity<AdminClassDtos.ClassDetailResponse> detail(
            @RequestParam Long schoolId,
            @PathVariable Long classId) {
        return ResponseEntity.ok(classService.getClassDetail(schoolId, classId));
    }

    @GetMapping("/{classId}/form-options")
    public ResponseEntity<AdminClassDtos.FormOptionsResponse> formOptions(
            @RequestParam Long schoolId,
            @PathVariable Long classId) {
        return ResponseEntity.ok(classService.getFormOptions(schoolId, classId));
    }

    // ─── Sections tab ─────────────────────────────────────────────────────────────

    @GetMapping("/{classId}/sections")
    public ResponseEntity<AdminClassDtos.SectionsTabResponse> sections(
            @RequestParam Long schoolId,
            @PathVariable Long classId) {
        return ResponseEntity.ok(classService.getSections(schoolId, classId));
    }

    @PostMapping("/{classId}/sections")
    public ResponseEntity<AdminClassDtos.ActionResponse> addSection(
            @PathVariable Long classId,
            @Valid @RequestBody AdminClassRequests.AddSectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classService.addSection(classId, request));
    }

    @PutMapping("/{classId}/sections/{sectionId}")
    public ResponseEntity<AdminClassDtos.ActionResponse> updateSection(
            @PathVariable Long classId,
            @PathVariable Long sectionId,
            @Valid @RequestBody AdminClassRequests.UpdateSectionRequest request) {
        return ResponseEntity.ok(classService.updateSection(classId, sectionId, request));
    }

    @DeleteMapping("/{classId}/sections/{sectionId}")
    public ResponseEntity<AdminClassDtos.ActionResponse> deleteSection(
            @RequestParam Long schoolId,
            @PathVariable Long classId,
            @PathVariable Long sectionId,
            @RequestParam(required = false) String performedBy) {
        return ResponseEntity.ok(classService.deleteSection(schoolId, classId, sectionId, performedBy));
    }

    // ─── Subjects tab ─────────────────────────────────────────────────────────────

    @GetMapping("/{classId}/subjects")
    public ResponseEntity<AdminClassDtos.SubjectsTabResponse> subjects(
            @RequestParam Long schoolId,
            @PathVariable Long classId) {
        return ResponseEntity.ok(classService.getSubjects(schoolId, classId));
    }

    @PostMapping("/{classId}/subjects")
    public ResponseEntity<AdminClassDtos.ActionResponse> addSubject(
            @PathVariable Long classId,
            @Valid @RequestBody AdminClassRequests.AddSubjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classService.addSubject(classId, request));
    }

    @PutMapping("/{classId}/subjects/{classSubjectId}")
    public ResponseEntity<AdminClassDtos.ActionResponse> updateSubject(
            @PathVariable Long classId,
            @PathVariable Long classSubjectId,
            @Valid @RequestBody AdminClassRequests.UpdateSubjectRequest request) {
        return ResponseEntity.ok(classService.updateSubject(classId, classSubjectId, request));
    }

    @DeleteMapping("/{classId}/subjects/{classSubjectId}")
    public ResponseEntity<AdminClassDtos.ActionResponse> deleteSubject(
            @RequestParam Long schoolId,
            @PathVariable Long classId,
            @PathVariable Long classSubjectId,
            @RequestParam(required = false) String performedBy) {
        return ResponseEntity.ok(classService.deleteSubject(schoolId, classId, classSubjectId, performedBy));
    }

    // ─── Teachers tab ─────────────────────────────────────────────────────────────

    @GetMapping("/{classId}/teachers")
    public ResponseEntity<AdminClassDtos.TeachersTabResponse> teachers(
            @RequestParam Long schoolId,
            @PathVariable Long classId) {
        return ResponseEntity.ok(classService.getTeacherAllocations(schoolId, classId));
    }

    @PostMapping("/{classId}/teachers")
    public ResponseEntity<AdminClassDtos.ActionResponse> assignTeacher(
            @PathVariable Long classId,
            @Valid @RequestBody AdminClassRequests.AssignTeacherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classService.assignTeacher(classId, request));
    }

    @DeleteMapping("/{classId}/teachers/{allocationId}")
    public ResponseEntity<AdminClassDtos.ActionResponse> removeAllocation(
            @RequestParam Long schoolId,
            @PathVariable Long classId,
            @PathVariable Long allocationId,
            @RequestParam(required = false) String performedBy) {
        return ResponseEntity.ok(classService.removeAllocation(schoolId, classId, allocationId, performedBy));
    }

    @PostMapping("/{classId}/assign-class-teacher")
    public ResponseEntity<AdminClassDtos.ActionResponse> assignClassTeacher(
            @PathVariable Long classId,
            @Valid @RequestBody AdminClassRequests.AssignClassTeacherRequest request) {
        return ResponseEntity.ok(classService.assignClassTeacher(classId, request));
    }

    // ─── Students tab ─────────────────────────────────────────────────────────────

    @GetMapping("/{classId}/students")
    public ResponseEntity<AdminClassDtos.StudentsTabResponse> students(
            @RequestParam Long schoolId,
            @PathVariable Long classId,
            @RequestParam(required = false) String section,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(classService.getStudents(schoolId, classId, section, page, size));
    }

    @GetMapping("/{classId}/students/search")
    public ResponseEntity<List<AdminClassDtos.StudentSearchResult>> searchStudents(
            @RequestParam Long schoolId,
            @PathVariable Long classId,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(classService.searchAddableStudents(schoolId, classId, q));
    }

    @PostMapping("/{classId}/students")
    public ResponseEntity<AdminClassDtos.ActionResponse> addStudent(
            @PathVariable Long classId,
            @Valid @RequestBody AdminClassRequests.AddStudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classService.addStudent(classId, request));
    }

    @PatchMapping("/{classId}/students/{studentId}/transfer")
    public ResponseEntity<AdminClassDtos.ActionResponse> transferStudent(
            @PathVariable Long classId,
            @PathVariable Long studentId,
            @Valid @RequestBody AdminClassRequests.TransferStudentRequest request) {
        return ResponseEntity.ok(classService.transferStudent(classId, studentId, request));
    }

    @DeleteMapping("/{classId}/students/{studentId}")
    public ResponseEntity<AdminClassDtos.ActionResponse> removeStudent(
            @RequestParam Long schoolId,
            @PathVariable Long classId,
            @PathVariable Long studentId,
            @RequestParam(required = false) String performedBy) {
        return ResponseEntity.ok(classService.removeStudent(schoolId, classId, studentId, performedBy));
    }

    @GetMapping("/students/bulk-template")
    public ResponseEntity<byte[]> bulkTemplate() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=class_students_template.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(classService.getBulkTemplate());
    }

    @PostMapping(value = "/{classId}/students/bulk-preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminClassDtos.BulkPreviewResponse> bulkPreview(
            @PathVariable Long classId,
            @RequestParam Long schoolId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(classService.previewBulkUpload(schoolId, classId, file));
    }

    @PostMapping(value = "/{classId}/students/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminClassDtos.BulkUploadResult> bulkUpload(
            @PathVariable Long classId,
            @RequestParam Long schoolId,
            @RequestParam(required = false) String performedBy,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(classService.bulkUpload(schoolId, classId, performedBy, file));
    }

    // ─── Activity log tab ─────────────────────────────────────────────────────────

    @GetMapping("/{classId}/activity-log")
    public ResponseEntity<AdminClassDtos.ActivityLogTabResponse> activityLog(
            @RequestParam Long schoolId,
            @PathVariable Long classId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(classService.getActivityLog(schoolId, classId, page, size));
    }
}
