package com.org.careerbuilder.controller;



import com.org.careerbuilder.dto.request.AdminStudentRequests;

import com.org.careerbuilder.dto.response.AdminStudentCertificateDtos;

import com.org.careerbuilder.dto.response.AdminStudentProfileDtos;

import com.org.careerbuilder.security.AdminSecurityContext;

import com.org.careerbuilder.service.AdminStudentActivityLogService;

import com.org.careerbuilder.service.AdminStudentCertificateService;

import com.org.careerbuilder.service.AdminStudentManagementService;

import com.org.careerbuilder.service.AdminStudentProfileService;

import com.org.careerbuilder.service.AdminStudentProfileTabsService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.HttpHeaders;

import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;



import java.time.LocalDate;



/**

 * Student profile page APIs (view, tabs, actions, documents, export).

 * Base path: /api/admin/students/{studentId}

 */

@RestController

@RequestMapping("/api/admin/students/{studentId}")

@RequiredArgsConstructor

@PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL_ADMIN')")

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},

        allowedHeaders = "*",

        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH,

                RequestMethod.DELETE, RequestMethod.OPTIONS})

public class AdminStudentProfileController {



    private final AdminStudentProfileService profileService;

    private final AdminStudentProfileTabsService profileTabsService;

    private final AdminStudentCertificateService certificateService;

    private final AdminStudentActivityLogService activityLogService;



    private Long schoolId() {

        return AdminSecurityContext.requireSchoolId();

    }



    // ─── Profile page & tabs ────────────────────────────────────────────────────



    @GetMapping("/profile")

    public ResponseEntity<AdminStudentProfileDtos.FullProfileResponse> fullProfile(

            @PathVariable Long studentId) {

        return ResponseEntity.ok(profileService.getFullProfile(schoolId(), studentId));

    }



    @GetMapping("/profile/performance")

    public ResponseEntity<AdminStudentProfileDtos.PerformanceTabResponse> performanceTab(

            @PathVariable Long studentId,

            @RequestParam(required = false) String examType,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(profileService.getPerformanceTab(schoolId(), studentId, examType, from, to));

    }



    @GetMapping("/profile/attendance")

    public ResponseEntity<AdminStudentProfileDtos.AttendanceTabResponse> attendanceTab(

            @PathVariable Long studentId,

            @RequestParam(required = false) String period,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(profileService.getAttendanceTab(schoolId(), studentId, period, from, to));

    }



    @GetMapping("/profile/fees")

    public ResponseEntity<AdminStudentProfileDtos.FeesTabResponse> feesTab(@PathVariable Long studentId) {

        return ResponseEntity.ok(profileService.getFeesTab(schoolId(), studentId));

    }



    @GetMapping("/profile/certificates")

    public ResponseEntity<AdminStudentProfileDtos.CertificatesTabResponse> certificatesTab(

            @PathVariable Long studentId,

            @RequestParam(required = false) String academicYear) {

        return ResponseEntity.ok(profileService.getCertificatesTab(schoolId(), studentId, academicYear));

    }



    @GetMapping("/profile/activity-log")

    public ResponseEntity<AdminStudentProfileDtos.ActivityLogTabResponse> activityLogTab(

            @PathVariable Long studentId,

            @RequestParam(required = false) String period,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(profileService.getActivityLogTab(schoolId(), studentId, period, from, to));

    }



    @GetMapping("/profile/study-activity")

    public ResponseEntity<AdminStudentProfileDtos.StudyActivityTabResponse> studyActivityTab(

            @PathVariable Long studentId,

            @RequestParam(required = false) String period,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(profileService.getStudyActivityTab(schoolId(), studentId, period, from, to));

    }



    // ─── Header actions ─────────────────────────────────────────────────────────



    @PostMapping("/message")

    public ResponseEntity<AdminStudentProfileDtos.MessageSentResponse> sendMessage(

            @PathVariable Long studentId,

            @Valid @RequestBody AdminStudentRequests.SendMessageRequest request) {

        AdminSecurityContext.stamp(request);

        return ResponseEntity.ok(profileService.sendMessage(studentId, request));

    }



    @PatchMapping("/quick-edit")

    public ResponseEntity<AdminStudentProfileDtos.ActionResponse> quickEdit(

            @PathVariable Long studentId,

            @Valid @RequestBody AdminStudentRequests.QuickEditRequest request) {

        AdminSecurityContext.stamp(request);

        return ResponseEntity.ok(profileService.quickEdit(studentId, request));

    }



    @PostMapping("/promote")

    public ResponseEntity<AdminStudentProfileDtos.ActionResponse> promote(

            @PathVariable Long studentId) {

        return ResponseEntity.ok(profileService.promoteStudent(

                schoolId(), studentId, AdminSecurityContext.performerName()));

    }



    @PostMapping("/transfer")

    public ResponseEntity<AdminStudentProfileDtos.ActionResponse> transfer(

            @PathVariable Long studentId,

            @Valid @RequestBody AdminStudentRequests.TransferStudentRequest request) {

        AdminSecurityContext.stamp(request);

        return ResponseEntity.ok(profileService.transferStudent(studentId, request));

    }



    @PostMapping("/deactivate")

    public ResponseEntity<AdminStudentProfileDtos.ActionResponse> deactivate(@PathVariable Long studentId) {

        return ResponseEntity.ok(profileService.deactivateStudent(

                schoolId(), studentId, AdminSecurityContext.performerName()));

    }



    @PostMapping("/reactivate")

    public ResponseEntity<AdminStudentProfileDtos.ActionResponse> reactivate(@PathVariable Long studentId) {

        return ResponseEntity.ok(profileService.reactivateStudent(

                schoolId(), studentId, AdminSecurityContext.performerName()));

    }



    @PostMapping("/flag")

    public ResponseEntity<AdminStudentProfileDtos.ActionResponse> flag(

            @PathVariable Long studentId,

            @Valid @RequestBody AdminStudentRequests.FlagStudentRequest request) {

        AdminSecurityContext.stamp(request);

        return ResponseEntity.ok(profileService.flagStudent(studentId, request));

    }



    // ─── Documents ──────────────────────────────────────────────────────────────



    @GetMapping("/documents")

    public ResponseEntity<java.util.List<AdminStudentProfileDtos.DocumentRow>> listDocuments(

            @PathVariable Long studentId) {

        return ResponseEntity.ok(profileService.getFullProfile(schoolId(), studentId).documents());

    }



    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    public ResponseEntity<AdminStudentProfileDtos.DocumentRow> uploadDocument(

            @PathVariable Long studentId,

            @RequestPart("file") MultipartFile file,

            @RequestParam(required = false) String documentType,

            @RequestParam(required = false) String uploadedBy) {

        AdminStudentRequests.UploadStudentDocumentRequest meta = AdminStudentRequests.UploadStudentDocumentRequest.builder()

                .documentType(documentType)

                .uploadedBy(uploadedBy)

                .build();

        AdminSecurityContext.stamp(meta);

        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)

                .body(profileService.uploadDocument(schoolId(), studentId, file, meta));

    }



    @DeleteMapping("/documents/{documentId}")

    public ResponseEntity<Void> deleteDocument(

            @PathVariable Long studentId,

            @PathVariable Long documentId) {

        profileService.deleteDocument(schoolId(), studentId, documentId);

        return ResponseEntity.noContent().build();

    }



    @GetMapping("/documents/{documentId}/download")

    public ResponseEntity<byte[]> downloadDocument(

            @PathVariable Long studentId,

            @PathVariable Long documentId) {

        byte[] content = profileService.downloadDocument(schoolId(), studentId, documentId);

        return ResponseEntity.ok()

                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=document")

                .body(content);

    }



    // ─── Export profile PDF / Excel / CSV ───────────────────────────────────────



    @GetMapping("/profile/export/catalog")

    public ResponseEntity<AdminStudentProfileDtos.ProfileExportCatalogResponse> exportCatalog() {

        return ResponseEntity.ok(profileService.getExportCatalog());

    }



    @PostMapping("/profile/export")

    public ResponseEntity<byte[]> exportProfile(

            @PathVariable Long studentId,

            @Valid @RequestBody AdminStudentRequests.ProfileExportRequest request) {

        AdminSecurityContext.stamp(request);

        AdminStudentManagementService.AdminStudentExportResult result =

                profileService.exportProfile(studentId, request);

        return ResponseEntity.ok()

                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.fileName())

                .contentType(MediaType.parseMediaType(result.contentType()))

                .body(result.content());

    }



    // ─── Performance tab ──────────────────────────────────────────────────────



    @GetMapping("/profile/performance/academic")

    public ResponseEntity<AdminStudentProfileDtos.AcademicPerformanceResponse> academicPerformance(

            @PathVariable Long studentId,

            @RequestParam(defaultValue = "QUIZ") String category,

            @RequestParam(required = false) String subjectSearch) {

        return ResponseEntity.ok(profileTabsService.getAcademicPerformance(

                schoolId(), studentId, category, subjectSearch));

    }



    @GetMapping("/profile/performance/export/catalog")

    public ResponseEntity<AdminStudentProfileDtos.PerformanceExportCatalogResponse> performanceExportCatalog() {

        return ResponseEntity.ok(profileTabsService.getPerformanceExportCatalog());

    }



    @PostMapping("/profile/performance/export")

    public ResponseEntity<byte[]> exportPerformance(

            @PathVariable Long studentId,

            @Valid @RequestBody AdminStudentRequests.PerformanceExportRequest request) {

        AdminSecurityContext.stamp(request);

        var result = profileTabsService.exportPerformance(studentId, request);

        return ResponseEntity.ok()

                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.fileName())

                .contentType(MediaType.parseMediaType(result.contentType()))

                .body(result.content());

    }



    @GetMapping("/assignments/{assignmentId}")

    public ResponseEntity<AdminStudentProfileDtos.AssignmentDetailResponse> assignmentDetail(

            @PathVariable Long studentId,

            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(profileTabsService.getAssignmentDetail(schoolId(), studentId, assignmentId));

    }



    // ─── Daily Activity tab ─────────────────────────────────────────────────────



    @GetMapping("/profile/daily-activity")

    public ResponseEntity<AdminStudentProfileDtos.DailyActivityTabResponse> dailyActivity(

            @PathVariable Long studentId,

            @RequestParam(required = false) String period,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @RequestParam(required = false) String search) {

        return ResponseEntity.ok(profileTabsService.getDailyActivityTab(

                schoolId(), studentId, period, from, to, search));

    }



    @PostMapping("/profile/daily-activity/report")

    public ResponseEntity<byte[]> dailyActivityReport(

            @PathVariable Long studentId,

            @Valid @RequestBody AdminStudentRequests.ReportDownloadRequest request) {

        AdminSecurityContext.stamp(request);

        var result = profileTabsService.exportDailyActivityReport(studentId, request);

        return ResponseEntity.ok()

                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.fileName())

                .contentType(MediaType.parseMediaType(result.contentType()))

                .body(result.content());

    }



    // ─── Attendance tab ─────────────────────────────────────────────────────────



    @GetMapping("/profile/attendance/summary")

    public ResponseEntity<AdminStudentProfileDtos.AttendanceSummaryTabResponse> attendanceSummary(

            @PathVariable Long studentId) {

        return ResponseEntity.ok(profileTabsService.getAttendanceSummaryTab(schoolId(), studentId));

    }



    @GetMapping("/profile/attendance/subjects/{subjectId}")

    public ResponseEntity<AdminStudentProfileDtos.SubjectAttendanceDetailResponse> subjectAttendanceDetail(

            @PathVariable Long studentId,

            @PathVariable Long subjectId,

            @RequestParam(required = false) String academicYear,

            @RequestParam(required = false) Integer month,

            @RequestParam(required = false) String status,

            @RequestParam(required = false) String search) {

        return ResponseEntity.ok(profileTabsService.getSubjectAttendanceDetail(

                schoolId(), studentId, subjectId, academicYear, month, status, search));

    }



    @PostMapping("/profile/attendance/subjects/{subjectId}/report")

    public ResponseEntity<byte[]> attendanceSubjectReport(

            @PathVariable Long studentId,

            @PathVariable Long subjectId,

            @Valid @RequestBody AdminStudentRequests.ReportDownloadRequest request) {

        AdminSecurityContext.stamp(request);

        var result = profileTabsService.exportAttendanceReport(studentId, subjectId, request);

        return ResponseEntity.ok()

                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.fileName())

                .contentType(MediaType.parseMediaType(result.contentType()))

                .body(result.content());

    }



    // ─── Fees tab ───────────────────────────────────────────────────────────────



    @GetMapping("/profile/fees/detail")

    public ResponseEntity<AdminStudentProfileDtos.FeesTabDetailResponse> feesDetail(@PathVariable Long studentId) {

        return ResponseEntity.ok(profileTabsService.getFeesTabDetail(schoolId(), studentId));

    }



    @PostMapping("/profile/fees/payment-link")

    public ResponseEntity<AdminStudentProfileDtos.PaymentLinkResponse> paymentLink(

            @PathVariable Long studentId,

            @Valid @RequestBody AdminStudentRequests.GeneratePaymentLinkRequest request) {

        AdminSecurityContext.stamp(request);

        return ResponseEntity.ok(profileTabsService.generatePaymentLink(studentId, request));

    }



    @PostMapping("/profile/fees/reminder")

    public ResponseEntity<AdminStudentProfileDtos.FeeReminderResponse> feeReminder(

            @PathVariable Long studentId,

            @Valid @RequestBody AdminStudentRequests.FeeReminderRequest request) {

        AdminSecurityContext.stamp(request);

        return ResponseEntity.ok(profileTabsService.sendFeeReminder(studentId, request));

    }



    @GetMapping("/profile/fees/statement")

    public ResponseEntity<byte[]> feeStatement(

            @PathVariable Long studentId,

            @RequestParam(defaultValue = "pdf") String format) {

        var result = profileTabsService.downloadFeeStatement(schoolId(), studentId, format);

        return ResponseEntity.ok()

                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.fileName())

                .contentType(MediaType.parseMediaType(result.contentType()))

                .body(result.content());

    }



    @GetMapping("/profile/fees/{feeId}/receipt")

    public ResponseEntity<byte[]> feeReceipt(

            @PathVariable Long studentId,

            @PathVariable Long feeId) {

        var result = profileTabsService.downloadFeeReceipt(schoolId(), studentId, feeId);

        return ResponseEntity.ok()

                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.fileName())

                .contentType(MediaType.parseMediaType(result.contentType()))

                .body(result.content());

    }



    // ─── Certificates tab ─────────────────────────────────────────────────────────



    @GetMapping("/profile/certificates/list")

    public ResponseEntity<AdminStudentCertificateDtos.CertificatesListResponse> listCertificates(

            @PathVariable Long studentId,

            @RequestParam(required = false) String search,

            @RequestParam(required = false) String category) {

        return ResponseEntity.ok(certificateService.listCertificates(schoolId(), studentId, search, category));

    }



    @GetMapping("/profile/certificates/{certificateId}")

    public ResponseEntity<AdminStudentCertificateDtos.CertificateDetailResponse> certificateDetail(

            @PathVariable Long studentId,

            @PathVariable Long certificateId) {

        return ResponseEntity.ok(certificateService.getCertificateDetail(schoolId(), studentId, certificateId));

    }



    @GetMapping("/profile/certificates/{certificateId}/download")

    public ResponseEntity<byte[]> downloadCertificate(

            @PathVariable Long studentId,

            @PathVariable Long certificateId) {

        byte[] content = certificateService.downloadCertificate(schoolId(), studentId, certificateId);

        return ResponseEntity.ok()

                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate")

                .body(content);

    }



    @PostMapping(value = "/profile/certificates", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    public ResponseEntity<AdminStudentCertificateDtos.CertificateUploadResponse> uploadCertificate(

            @PathVariable Long studentId,

            @RequestPart("file") MultipartFile file,

            @RequestParam String certificateName,

            @RequestParam String category,

            @RequestParam(required = false) String issueDate,

            @RequestParam(required = false) String academicYear,

            @RequestParam(required = false) String uploadedBy) {

        AdminStudentRequests.UploadCertificateRequest meta = AdminStudentRequests.UploadCertificateRequest.builder()

                .certificateName(certificateName)

                .category(category)

                .issueDate(issueDate)

                .academicYear(academicYear)

                .uploadedBy(uploadedBy)

                .build();

        AdminSecurityContext.stamp(meta);

        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)

                .body(certificateService.uploadCertificate(schoolId(), studentId, file, meta));

    }



    @DeleteMapping("/profile/certificates/{certificateId}")

    public ResponseEntity<Void> deleteCertificate(

            @PathVariable Long studentId,

            @PathVariable Long certificateId) {

        certificateService.deleteCertificate(

                schoolId(), studentId, certificateId, AdminSecurityContext.performerName());

        return ResponseEntity.noContent().build();

    }



    @GetMapping("/profile/certificates/bulk/template")

    public ResponseEntity<byte[]> certificateBulkTemplate() {

        byte[] content = certificateService.downloadBulkTemplate();

        return ResponseEntity.ok()

                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate_bulk_template.xlsx")

                .contentType(MediaType.parseMediaType(

                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))

                .body(content);

    }



    @PostMapping(value = "/profile/certificates/bulk/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    public ResponseEntity<AdminStudentCertificateDtos.BulkCertificatePreviewResponse> certificateBulkPreview(

            @PathVariable Long studentId,

            @RequestPart("excel") MultipartFile excelFile,

            @RequestPart(value = "filesArchive", required = false) MultipartFile filesArchive) {

        return ResponseEntity.ok(certificateService.previewBulkUpload(

                schoolId(), studentId, excelFile, filesArchive));

    }



    @PostMapping("/profile/certificates/bulk/commit/{previewId}")

    public ResponseEntity<AdminStudentCertificateDtos.BulkCertificateCommitResponse> certificateBulkCommit(

            @PathVariable Long studentId,

            @PathVariable String previewId) {

        return ResponseEntity.ok(certificateService.commitBulkUpload(

                schoolId(), studentId, previewId, AdminSecurityContext.performerName()));

    }



    // ─── Activity log tab ───────────────────────────────────────────────────────



    @GetMapping("/profile/activity-log/feed")

    public ResponseEntity<AdminStudentCertificateDtos.ActivityLogFeedResponse> activityLogFeed(

            @PathVariable Long studentId,

            @RequestParam(required = false) String search,

            @RequestParam(required = false) String logType,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(activityLogService.getActivityLog(

                schoolId(), studentId, search, logType, page, size));

    }



    @GetMapping("/profile/activity-log/filter-options")

    public ResponseEntity<AdminStudentCertificateDtos.ActivityLogFilterOptions> activityLogFilterOptions() {

        return ResponseEntity.ok(activityLogService.getFilterOptions());

    }

}

