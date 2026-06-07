package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.AdminTeacherProfileRequests;
import com.org.careerbuilder.dto.response.AdminTeacherDtos;
import com.org.careerbuilder.dto.response.AdminTeacherProfileDtos;
import com.org.careerbuilder.service.AdminTeacherManagementService;
import com.org.careerbuilder.service.AdminTeacherProfileService;
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
 * Teacher profile page — tabs, documents, export, header actions.
 * Base: /api/admin/teachers/{facultyId}/profile
 */
@RestController
@RequestMapping("/api/admin/teachers/{facultyId}/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH,
                RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AdminTeacherProfileController {

    private final AdminTeacherProfileService profileService;

    @GetMapping
    public ResponseEntity<AdminTeacherProfileDtos.FullProfileResponse> fullProfile(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId) {
        return ResponseEntity.ok(profileService.getFullProfile(schoolId, facultyId));
    }

    @GetMapping("/more-actions")
    public ResponseEntity<AdminTeacherProfileDtos.MoreActionsMenuResponse> moreActions(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId) {
        return ResponseEntity.ok(profileService.getMoreActionsMenu(schoolId, facultyId));
    }

    // ─── Tabs ───────────────────────────────────────────────────────────────────

    @GetMapping("/basic-details")
    public ResponseEntity<AdminTeacherProfileDtos.BasicDetailsTab> basicDetails(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId) {
        return ResponseEntity.ok(profileService.getBasicDetailsTab(schoolId, facultyId));
    }

    @GetMapping("/allocations")
    public ResponseEntity<AdminTeacherProfileDtos.AllocationsTabResponse> allocations(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId) {
        return ResponseEntity.ok(profileService.getAllocationsTab(schoolId, facultyId));
    }

    @GetMapping("/assignments")
    public ResponseEntity<AdminTeacherProfileDtos.AssignmentsTabResponse> assignments(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId) {
        return ResponseEntity.ok(profileService.getAssignmentsTab(schoolId, facultyId));
    }

    /** "View Assignment" detail — submission summary + per-student rows. */
    @GetMapping("/assignments/{assignmentId}")
    public ResponseEntity<AdminTeacherProfileDtos.AssignmentDetailResponse> assignmentDetail(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId,
            @PathVariable Long assignmentId) {
        return ResponseEntity.ok(profileService.getAssignmentDetail(schoolId, facultyId, assignmentId));
    }

    @GetMapping("/attendance-leave")
    public ResponseEntity<AdminTeacherProfileDtos.AttendanceLeaveTabResponse> attendanceLeave(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId) {
        return ResponseEntity.ok(profileService.getAttendanceLeaveTab(schoolId, facultyId));
    }

    /** Approve / reject a pending leave request (Review Leave Request modal). */
    @PostMapping("/leave/{leaveId}/review")
    public ResponseEntity<AdminTeacherDtos.ActionResponse> reviewLeave(
            @PathVariable Long facultyId,
            @PathVariable Long leaveId,
            @Valid @RequestBody AdminTeacherProfileRequests.ReviewLeaveRequest request) {
        return ResponseEntity.ok(profileService.reviewLeave(facultyId, leaveId, request));
    }

    @GetMapping("/workload")
    public ResponseEntity<AdminTeacherProfileDtos.WorkloadTabResponse> workload(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId) {
        return ResponseEntity.ok(profileService.getWorkloadTab(schoolId, facultyId));
    }

    @GetMapping("/employment")
    public ResponseEntity<AdminTeacherProfileDtos.EmploymentTabResponse> employment(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId) {
        return ResponseEntity.ok(profileService.getEmploymentTab(schoolId, facultyId));
    }

    @GetMapping("/employment-documents/{kind}")
    public ResponseEntity<byte[]> employmentDocument(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId,
            @PathVariable String kind) {
        byte[] content = profileService.downloadEmploymentDocument(schoolId, facultyId, kind);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + kind)
                .body(content);
    }

    @GetMapping("/activity-log")
    public ResponseEntity<AdminTeacherProfileDtos.ActivityLogTabResponse> activityLog(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(profileService.getActivityLogTab(schoolId, facultyId, page, size));
    }

    // ─── Documents ──────────────────────────────────────────────────────────────

    @GetMapping("/documents/types")
    public ResponseEntity<List<AdminTeacherProfileDtos.DocumentTypeOption>> documentTypes() {
        return ResponseEntity.ok(profileService.getDocumentTypes());
    }

    @GetMapping("/documents")
    public ResponseEntity<List<AdminTeacherProfileDtos.DocumentRow>> listDocuments(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId) {
        return ResponseEntity.ok(profileService.listDocuments(schoolId, facultyId));
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminTeacherProfileDtos.DocumentRow> uploadDocument(
            @PathVariable Long facultyId,
            @RequestPart("file") MultipartFile file,
            @RequestParam Long schoolId,
            @RequestParam String documentName,
            @RequestParam String documentType,
            @RequestParam(required = false) String uploadedBy) {
        var meta = AdminTeacherProfileRequests.UploadDocumentRequest.builder()
                .schoolId(schoolId)
                .documentName(documentName)
                .documentType(documentType)
                .uploadedBy(uploadedBy)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.uploadDocument(facultyId, file, meta));
    }

    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId,
            @PathVariable Long documentId) {
        profileService.deleteDocument(schoolId, facultyId, documentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<byte[]> downloadDocument(
            @RequestParam Long schoolId,
            @PathVariable Long facultyId,
            @PathVariable Long documentId) {
        byte[] content = profileService.downloadDocument(schoolId, facultyId, documentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=document")
                .body(content);
    }

    // ─── Export profile ─────────────────────────────────────────────────────────

    @GetMapping("/export/fields")
    public ResponseEntity<AdminTeacherProfileDtos.ProfileExportCatalogResponse> exportFields() {
        return ResponseEntity.ok(profileService.getProfileExportCatalog());
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> exportProfile(
            @PathVariable Long facultyId,
            @Valid @RequestBody AdminTeacherProfileRequests.ProfileExportRequest request) {
        AdminTeacherManagementService.AdminTeacherExportResult result =
                profileService.exportProfile(facultyId, request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.fileName())
                .contentType(MediaType.parseMediaType(result.contentType()))
                .body(result.content());
    }

    // ─── Header actions (⋯ menu) ────────────────────────────────────────────────

    @PostMapping("/mark-leave")
    public ResponseEntity<AdminTeacherDtos.ActionResponse> markLeave(
            @PathVariable Long facultyId,
            @Valid @RequestBody AdminTeacherProfileRequests.MarkLeaveRequest request) {
        return ResponseEntity.ok(profileService.markLeave(facultyId, request));
    }

    @PostMapping("/deactivate")
    public ResponseEntity<AdminTeacherDtos.ActionResponse> deactivate(
            @PathVariable Long facultyId,
            @Valid @RequestBody AdminTeacherProfileRequests.DeactivateRequest request) {
        return ResponseEntity.ok(profileService.deactivate(facultyId, request));
    }

    @PostMapping("/assign-subject")
    public ResponseEntity<AdminTeacherDtos.ActionResponse> assignSubject(
            @PathVariable Long facultyId,
            @Valid @RequestBody AdminTeacherProfileRequests.AssignSubjectRequest request) {
        return ResponseEntity.ok(profileService.assignSubject(facultyId, request));
    }
}
