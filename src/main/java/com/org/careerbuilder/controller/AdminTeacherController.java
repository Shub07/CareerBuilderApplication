package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.AdminTeacherRequests;
import com.org.careerbuilder.dto.response.AdminOperationResponses;
import com.org.careerbuilder.dto.response.AdminTeacherDtos;
import com.org.careerbuilder.service.AdminTeacherManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Admin Teachers portal — list, create, update, assignments.
 * Base path: /api/admin/teachers
 */
@RestController
@RequestMapping("/api/admin/teachers")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH,
                RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AdminTeacherController {

    private final AdminTeacherManagementService teacherService;

    // ─── Dashboard & list ─────────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<AdminTeacherDtos.TeacherStats> stats(@RequestParam Long schoolId) {
        return ResponseEntity.ok(teacherService.getStats(schoolId));
    }

    @GetMapping("/filters")
    public ResponseEntity<AdminTeacherDtos.TeacherFilterOptions> filters(@RequestParam Long schoolId) {
        return ResponseEntity.ok(teacherService.getFilterOptions(schoolId));
    }

    @GetMapping("/academic-years")
    public ResponseEntity<AdminTeacherDtos.AcademicYearsResponse> academicYears(@RequestParam Long schoolId) {
        return ResponseEntity.ok(teacherService.getAcademicYears(schoolId));
    }

    /**
     * Paginated teachers table — supports search, subject/status dropdowns,
     * and stat-card filter (statsFilter=ACTIVE|ON_LEAVE|INACTIVE|UNASSIGNED|ALL).
     */
    @GetMapping("/section")
    public ResponseEntity<AdminTeacherDtos.TeacherSectionResponse> section(
            @RequestParam Long schoolId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String statsFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(teacherService.listTeachers(
                schoolId, academicYear, q, subjectId, status, statsFilter, page, size));
    }

    @PostMapping("/section/query")
    public ResponseEntity<AdminTeacherDtos.TeacherSectionResponse> sectionQuery(
            @Valid @RequestBody AdminTeacherRequests.TeacherListQuery query) {
        return ResponseEntity.ok(teacherService.listTeachers(query));
    }

    // ─── Bulk actions (table checkboxes) ────────────────────────────────────────

    @PatchMapping("/bulk/status")
    public ResponseEntity<AdminOperationResponses.BulkActionResponse> bulkStatus(
            @Valid @RequestBody AdminTeacherRequests.BulkStatusRequest request) {
        return ResponseEntity.ok(teacherService.bulkChangeStatus(request));
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<AdminOperationResponses.BulkActionResponse> bulkDelete(
            @Valid @RequestBody AdminTeacherRequests.BulkDeleteRequest request) {
        return ResponseEntity.ok(teacherService.bulkDelete(request));
    }

    // ─── Export (filter-based & bulk selection) ───────────────────────────────

    @GetMapping("/export/fields")
    public ResponseEntity<AdminTeacherDtos.ExportFieldCatalogResponse> exportFields() {
        return ResponseEntity.ok(teacherService.getExportFieldCatalog());
    }

    @PostMapping("/export/preview")
    public ResponseEntity<AdminTeacherDtos.ExportPreviewResponse> exportPreview(
            @Valid @RequestBody AdminTeacherRequests.ExportRequest request) {
        return ResponseEntity.ok(teacherService.previewExport(request));
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> exportTeachers(
            @Valid @RequestBody AdminTeacherRequests.ExportRequest request) {
        AdminTeacherManagementService.AdminTeacherExportResult result =
                teacherService.exportTeachers(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.fileName())
                .contentType(MediaType.parseMediaType(result.contentType()))
                .body(result.content());
    }

    @GetMapping("/{facultyId}")
    public ResponseEntity<AdminTeacherDtos.TeacherDetailResponse> detail(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId) {
        return ResponseEntity.ok(teacherService.getTeacherDetail(schoolId, facultyId));
    }

    /** Row action: View Profile Summary */
    @GetMapping("/{facultyId}/summary")
    public ResponseEntity<AdminTeacherDtos.TeacherProfileSummaryResponse> profileSummary(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId) {
        return ResponseEntity.ok(teacherService.getProfileSummary(schoolId, facultyId));
    }

    /** Row action: Edit Details — pre-filled form */
    @GetMapping("/{facultyId}/edit-form")
    public ResponseEntity<AdminTeacherDtos.EditFormResponse> editForm(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId) {
        return ResponseEntity.ok(teacherService.getEditForm(schoolId, facultyId));
    }

    /** Row action: Delete Teacher — confirmation modal copy */
    @GetMapping("/{facultyId}/delete-preview")
    public ResponseEntity<AdminTeacherDtos.DeletePreviewResponse> deletePreview(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId) {
        return ResponseEntity.ok(teacherService.getDeletePreview(schoolId, facultyId));
    }

    /** Row action: Allocate Classes — form data */
    @GetMapping("/{facultyId}/allocations/form")
    public ResponseEntity<AdminTeacherDtos.AllocateClassesFormResponse> allocateClassesForm(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId) {
        return ResponseEntity.ok(teacherService.getAllocateClassesForm(schoolId, facultyId));
    }

    /** Row action: Allocate Classes — save */
    @PostMapping("/{facultyId}/allocations")
    public ResponseEntity<AdminTeacherDtos.AllocateClassesResponse> allocateClasses(
            @PathVariable Long facultyId,
            @Valid @RequestBody AdminTeacherRequests.AllocateClassesRequest request) {
        return ResponseEntity.ok(teacherService.allocateClasses(facultyId, request));
    }

    // ─── Create / update ──────────────────────────────────────────────────────

    /** JSON create (Create Teacher modal without files). */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminTeacherDtos.TeacherCreatedResponse> create(
            @Valid @RequestBody AdminTeacherRequests.CreateTeacherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teacherService.createTeacher(request, null, null, null));
    }

    /** Multipart create with photo, resume, ID proof. */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminTeacherDtos.TeacherCreatedResponse> createWithFiles(
            @RequestPart("data") @Valid AdminTeacherRequests.CreateTeacherRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            @RequestPart(value = "resume", required = false) MultipartFile resume,
            @RequestPart(value = "idProof", required = false) MultipartFile idProof) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teacherService.createTeacher(request, photo, resume, idProof));
    }

    @PutMapping(value = "/{facultyId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminTeacherDtos.TeacherDetailResponse> updateMultipart(
            @PathVariable Long facultyId,
            @RequestPart("data") @Valid AdminTeacherRequests.UpdateTeacherRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            @RequestPart(value = "resume", required = false) MultipartFile resume,
            @RequestPart(value = "idProof", required = false) MultipartFile idProof,
            @RequestPart(value = "certificates", required = false) MultipartFile certificates,
            @RequestPart(value = "experienceLetters", required = false) MultipartFile experienceLetters) {
        return ResponseEntity.ok(teacherService.updateTeacher(
                facultyId, request, photo, resume, idProof, certificates, experienceLetters));
    }

    @PutMapping(value = "/{facultyId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminTeacherDtos.TeacherDetailResponse> update(
            @PathVariable Long facultyId,
            @Valid @RequestBody AdminTeacherRequests.UpdateTeacherRequest request) {
        return ResponseEntity.ok(teacherService.updateTeacher(
                facultyId, request, null, null, null, null, null));
    }

    @PatchMapping("/{facultyId}/status")
    public ResponseEntity<AdminTeacherDtos.ActionResponse> updateStatus(
            @PathVariable Long facultyId,
            @Valid @RequestBody AdminTeacherRequests.UpdateStatusRequest request) {
        return ResponseEntity.ok(teacherService.updateStatus(facultyId, request));
    }

    /** Row action: Delete Teacher — confirm deletion */
    @DeleteMapping("/{facultyId}")
    public ResponseEntity<AdminTeacherDtos.DeleteTeacherResponse> delete(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId,
            @RequestParam(required = false) String performedBy) {
        return ResponseEntity.ok(teacherService.deleteTeacher(schoolId, facultyId, performedBy));
    }

    @PostMapping("/{facultyId}/delete")
    public ResponseEntity<AdminTeacherDtos.DeleteTeacherResponse> confirmDelete(
            @PathVariable Long facultyId,
            @Valid @RequestBody AdminTeacherRequests.ConfirmDeleteRequest request) {
        return ResponseEntity.ok(teacherService.deleteTeacher(
                request.getSchoolId(), facultyId, request.getPerformedBy()));
    }

    // ─── Class assignments ────────────────────────────────────────────────────

    @PostMapping("/{facultyId}/assignments")
    public ResponseEntity<AdminTeacherDtos.AssignmentCreatedResponse> addAssignment(
            @PathVariable Long facultyId,
            @Valid @RequestBody AdminTeacherRequests.AddAssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teacherService.addAssignment(facultyId, request));
    }

    @DeleteMapping("/{facultyId}/assignments/{assignmentId}")
    public ResponseEntity<AdminTeacherDtos.ActionResponse> removeAssignment(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId,
            @PathVariable Long assignmentId,
            @RequestParam(required = false) String performedBy) {
        return ResponseEntity.ok(teacherService.removeAssignment(schoolId, facultyId, assignmentId, performedBy));
    }
}
