package com.org.careerbuilder.controller;



import com.org.careerbuilder.dto.request.AdminStudentRegistrationRequest;

import com.org.careerbuilder.dto.request.AdminStudentRequests;

import com.org.careerbuilder.dto.response.AdminOperationResponses;

import com.org.careerbuilder.security.AdminSecurityContext;

import com.org.careerbuilder.service.AdminStudentManagementService;

import com.org.careerbuilder.service.AdminStudentRegistrationService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.HttpStatus;

import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;



import java.time.LocalDate;

import java.util.Arrays;

import java.util.List;

import java.util.Map;



@RestController

@RequestMapping("/api/admin/students")

@RequiredArgsConstructor

@PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL_ADMIN')")

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},

        allowedHeaders = "*",

        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH,

                RequestMethod.DELETE, RequestMethod.OPTIONS})

public class AdminStudentController {



    private final AdminStudentRegistrationService registrationService;

    private final AdminStudentManagementService managementService;



    // ─── Dashboard & list ─────────────────────────────────────────────────────



    @GetMapping("/stats")

    public ResponseEntity<AdminOperationResponses.StudentStats> stats(

            @RequestParam(required = false) String academicYear) {

        return ResponseEntity.ok(managementService.getStats(

                AdminSecurityContext.requireSchoolId(), academicYear));

    }



    @GetMapping("/academic-years")

    public ResponseEntity<AdminOperationResponses.AcademicYearsResponse> academicYears() {

        return ResponseEntity.ok(managementService.getAcademicYears(AdminSecurityContext.requireSchoolId()));

    }



    @GetMapping("/filters")

    public ResponseEntity<AdminOperationResponses.StudentFilterOptionsResponse> filters() {

        return ResponseEntity.ok(managementService.getFilterOptions(AdminSecurityContext.requireSchoolId()));

    }



    @GetMapping("/advanced-filters/options")

    public ResponseEntity<AdminOperationResponses.AdvancedFilterOptionsResponse> advancedFilterOptions() {

        return ResponseEntity.ok(managementService.getAdvancedFilterOptions());

    }



    @GetMapping("/preferences/columns")

    public ResponseEntity<AdminOperationResponses.TablePreferencesResponse> tablePreferences(

            @RequestParam(required = false) Long adminUserId,

            @RequestParam(required = false) String adminEmail) {

        Long schoolId = AdminSecurityContext.requireSchoolId();

        return ResponseEntity.ok(managementService.getTablePreferences(

                schoolId,

                AdminSecurityContext.resolveAdminUserId(adminUserId),

                AdminSecurityContext.resolveAdminEmail(adminEmail)));

    }



    @PutMapping("/preferences/columns")

    public ResponseEntity<AdminOperationResponses.TablePreferencesResponse> saveTablePreferences(

            @Valid @RequestBody AdminStudentRequests.SaveTablePreferencesRequest request) {

        AdminSecurityContext.stamp(request);

        return ResponseEntity.ok(managementService.saveTablePreferences(request));

    }



    @GetMapping("/section")

    public ResponseEntity<AdminOperationResponses.StudentSectionResponse> section(

            @RequestParam(required = false) String academicYear,

            @RequestParam(required = false) String q,

            @RequestParam(required = false) String className,

            @RequestParam(required = false) String section,

            @RequestParam(required = false) String gender,

            @RequestParam(required = false) String status,

            @RequestParam(required = false) String feeStatus,

            @RequestParam(required = false) List<String> feeStatuses,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate admissionDateFrom,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate admissionDateTo,

            @RequestParam(required = false) String performanceLevel,

            @RequestParam(required = false) Integer minAttendance,

            @RequestParam(required = false) Integer maxAttendance,

            @RequestParam(required = false) Long adminUserId,

            @RequestParam(required = false) String adminEmail,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(managementService.listStudents(buildListQuery(

                AdminSecurityContext.requireSchoolId(), academicYear, q, className, section, gender, status,

                feeStatus, feeStatuses, admissionDateFrom, admissionDateTo, performanceLevel, minAttendance,

                maxAttendance, adminUserId, adminEmail, page, size)));

    }



    @PostMapping("/section/query")

    public ResponseEntity<AdminOperationResponses.StudentSectionResponse> sectionQuery(

            @Valid @RequestBody AdminStudentRequests.StudentListQuery query) {

        AdminSecurityContext.stamp(query);

        return ResponseEntity.ok(managementService.listStudents(query));

    }



    @GetMapping("/{studentId}")

    public ResponseEntity<AdminOperationResponses.StudentDetailResponse> detail(@PathVariable Long studentId) {

        return ResponseEntity.ok(managementService.getStudentDetail(

                AdminSecurityContext.requireSchoolId(), studentId));

    }



    @PutMapping("/{studentId}")

    public ResponseEntity<AdminOperationResponses.StudentDetailResponse> update(

            @PathVariable Long studentId,

            @Valid @RequestBody AdminStudentRequests.UpdateStudentRequest request) {

        AdminSecurityContext.stamp(request);

        return ResponseEntity.ok(managementService.updateStudent(

                AdminSecurityContext.requireSchoolId(), studentId, request));

    }



    @PatchMapping("/{studentId}/status")

    public ResponseEntity<AdminOperationResponses.BulkActionResponse> updateStatus(

            @PathVariable Long studentId,

            @Valid @RequestBody AdminStudentRequests.UpdateStatusRequest request) {

        AdminSecurityContext.stamp(request);

        return ResponseEntity.ok(managementService.updateStudentStatus(

                AdminSecurityContext.requireSchoolId(), studentId, request));

    }



    @DeleteMapping("/{studentId}")

    public ResponseEntity<Void> deleteOne(@PathVariable Long studentId) {

        managementService.deleteStudent(

                AdminSecurityContext.requireSchoolId(), studentId, AdminSecurityContext.performerName());

        return ResponseEntity.noContent().build();

    }



    // ─── Bulk actions ───────────────────────────────────────────────────────────



    @PatchMapping("/bulk/status")

    public ResponseEntity<AdminOperationResponses.BulkActionResponse> bulkStatus(

            @Valid @RequestBody AdminStudentRequests.BulkStatusRequest request) {

        AdminSecurityContext.stamp(request);

        return ResponseEntity.ok(managementService.bulkChangeStatus(request));

    }



    @PostMapping("/bulk/promote")

    public ResponseEntity<AdminOperationResponses.BulkActionResponse> bulkPromote(

            @Valid @RequestBody AdminStudentRequests.BulkPromoteRequest request) {

        AdminSecurityContext.stamp(request);

        return ResponseEntity.ok(managementService.bulkPromote(request));

    }



    @DeleteMapping("/bulk")

    public ResponseEntity<AdminOperationResponses.BulkActionResponse> bulkDelete(

            @Valid @RequestBody AdminStudentRequests.BulkDeleteRequest request) {

        AdminSecurityContext.stamp(request);

        return ResponseEntity.ok(managementService.bulkDelete(request));

    }



    // ─── Export ─────────────────────────────────────────────────────────────────



    @GetMapping("/export/fields")

    public ResponseEntity<AdminOperationResponses.ExportFieldCatalogResponse> exportFields() {

        return ResponseEntity.ok(managementService.getExportFieldCatalog());

    }



    @PostMapping("/export")

    public ResponseEntity<byte[]> export(@Valid @RequestBody AdminStudentRequests.ExportRequest request) {

        AdminSecurityContext.stamp(request);

        AdminStudentManagementService.AdminStudentExportResult result =

                managementService.exportStudents(request);

        return ResponseEntity.ok()

                .header("Content-Disposition", "attachment; filename=" + result.fileName())

                .contentType(MediaType.parseMediaType(result.contentType()))

                .body(result.content());

    }



    // ─── Registration & bulk register ───────────────────────────────────────────



    @GetMapping("/registration/metadata")

    public ResponseEntity<AdminOperationResponses.RegistrationMetadataResponse> metadata() {

        return ResponseEntity.ok(registrationService.getMetadata(AdminSecurityContext.requireSchoolId()));

    }



    @PostMapping("/register")

    public ResponseEntity<AdminOperationResponses.StudentRegistrationResponse> register(

            @Valid @RequestBody AdminStudentRegistrationRequest request) {

        AdminSecurityContext.stamp(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.register(request));

    }



    @GetMapping("/bulk/template")

    public ResponseEntity<byte[]> downloadTemplate() {

        byte[] content = registrationService.generateBulkTemplate();

        return ResponseEntity.ok()

                .header("Content-Disposition", "attachment; filename=student-bulk-template.xlsx")

                .contentType(MediaType.parseMediaType(

                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))

                .body(content);

    }



    @PostMapping(value = "/bulk/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    public ResponseEntity<AdminOperationResponses.BulkPreviewResponse> previewBulk(

            @RequestPart("file") MultipartFile file) {

        return ResponseEntity.ok(registrationService.previewBulkUpload(

                AdminSecurityContext.requireSchoolId(), file));

    }



    @PostMapping("/bulk/commit/{previewId}")

    public ResponseEntity<AdminOperationResponses.BulkCommitResponse> commitBulk(

            @PathVariable String previewId) {

        return ResponseEntity.status(HttpStatus.CREATED)

                .body(registrationService.commitBulkUpload(previewId, AdminSecurityContext.performerName()));

    }



    @PostMapping(value = "/upload/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    public ResponseEntity<Map<String, String>> uploadPhoto(@RequestPart("file") MultipartFile file) {

        String url = registrationService.uploadPhoto(AdminSecurityContext.requireSchoolId(), file);

        return ResponseEntity.ok(Map.of("url", url));

    }



    @PostMapping(value = "/upload/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    public ResponseEntity<Map<String, String>> uploadDocument(

            @RequestParam String documentType,

            @RequestPart("file") MultipartFile file) {

        String url = registrationService.uploadDocument(

                AdminSecurityContext.requireSchoolId(), documentType, file);

        return ResponseEntity.ok(Map.of("url", url, "documentType", documentType));

    }



    private static AdminStudentRequests.StudentListQuery buildListQuery(

            Long schoolId,

            String academicYear,

            String q,

            String className,

            String section,

            String gender,

            String status,

            String feeStatus,

            List<String> feeStatuses,

            LocalDate admissionDateFrom,

            LocalDate admissionDateTo,

            String performanceLevel,

            Integer minAttendance,

            Integer maxAttendance,

            Long adminUserId,

            String adminEmail,

            int page,

            int size) {

        List<String> resolvedFeeStatuses = feeStatuses;

        if ((resolvedFeeStatuses == null || resolvedFeeStatuses.isEmpty()) && feeStatus != null && feeStatus.contains(",")) {

            resolvedFeeStatuses = Arrays.stream(feeStatus.split(",")).map(String::trim).toList();

        }

        return AdminStudentRequests.StudentListQuery.builder()

                .schoolId(schoolId)

                .academicYear(academicYear)

                .query(q)

                .className(className)

                .section(section)

                .gender(gender)

                .status(status)

                .feeStatus(feeStatus != null && feeStatus.contains(",") ? null : feeStatus)

                .feeStatuses(resolvedFeeStatuses)

                .admissionDateFrom(admissionDateFrom)

                .admissionDateTo(admissionDateTo)

                .performanceLevel(performanceLevel)

                .minAttendance(minAttendance)

                .maxAttendance(maxAttendance)

                .adminUserId(AdminSecurityContext.resolveAdminUserId(adminUserId))

                .adminEmail(AdminSecurityContext.resolveAdminEmail(adminEmail))

                .page(page)

                .size(size)

                .build();

    }

}

