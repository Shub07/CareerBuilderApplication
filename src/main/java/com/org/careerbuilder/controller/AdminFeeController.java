package com.org.careerbuilder.controller;



import com.org.careerbuilder.dto.request.AdminFeeRequests;

import com.org.careerbuilder.dto.response.AdminFeeDtos;

import com.org.careerbuilder.models.FeeJob;

import com.org.careerbuilder.security.AdminSecurityContext;

import com.org.careerbuilder.service.AdminFeeService;

import com.org.careerbuilder.service.FeeJobService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;

import org.springframework.http.HttpStatus;

import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;



import java.util.List;



@RestController

@RequestMapping("/api/admin/fees")

@RequiredArgsConstructor

@PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL_ADMIN')")

public class AdminFeeController {



    private final AdminFeeService feeService;

    private final FeeJobService feeJobService;



    // ─── List page ──────────────────────────────────────────────────────────────



    @GetMapping("/stats")

    public ResponseEntity<AdminFeeDtos.FeeStatsResponse> stats() {

        return ResponseEntity.ok(feeService.getStats(AdminSecurityContext.requireSchoolId()));

    }



    @GetMapping("/students")

    public ResponseEntity<AdminFeeDtos.StudentFeeListResponse> students(

            @RequestParam(required = false) String q,

            @RequestParam(name = "class", required = false) String className,

            @RequestParam(required = false) String status,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size) {

        Long schoolId = AdminSecurityContext.requireSchoolId();

        return ResponseEntity.ok(feeService.listStudentFees(schoolId, q, className, status, page, size));

    }



    @GetMapping("/filters")

    public ResponseEntity<AdminFeeDtos.FeeFilterOptionsResponse> filters() {

        return ResponseEntity.ok(feeService.getFilterOptions(AdminSecurityContext.requireSchoolId()));

    }



    @GetMapping("/students/search")

    public ResponseEntity<List<AdminFeeDtos.StudentSearchOption>> searchStudents(

            @RequestParam(required = false) String q) {

        return ResponseEntity.ok(feeService.searchStudents(AdminSecurityContext.requireSchoolId(), q));

    }



    // ─── Fee structures ─────────────────────────────────────────────────────────



    @GetMapping("/structures")

    public ResponseEntity<AdminFeeDtos.FeeStructureListResponse> structures() {

        return ResponseEntity.ok(feeService.getStructures(AdminSecurityContext.requireSchoolId()));

    }



    @PostMapping("/structures")

    public ResponseEntity<AdminFeeDtos.FeeStructureRow> createStructure(

            @Valid @RequestBody AdminFeeRequests.CreateFeeStructureRequest request) {

        AdminSecurityContext.stamp(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(feeService.createStructure(request));

    }



    @PutMapping("/structures/{structureId}")

    public ResponseEntity<AdminFeeDtos.FeeStructureRow> updateStructure(

            @PathVariable Long structureId,

            @Valid @RequestBody AdminFeeRequests.UpdateFeeStructureRequest request) {

        AdminSecurityContext.stamp(request);

        return ResponseEntity.ok(feeService.updateStructure(structureId, request));

    }



    @DeleteMapping("/structures/{structureId}")

    public ResponseEntity<AdminFeeDtos.ActionResponse> deleteStructure(@PathVariable Long structureId) {

        Long schoolId = AdminSecurityContext.requireSchoolId();

        return ResponseEntity.ok(feeService.deleteStructure(

                schoolId, structureId, AdminSecurityContext.performerName()));

    }



    // ─── Collect / refund ─────────────────────────────────────────────────────



    @GetMapping("/students/{studentId}/paid-fees")

    public ResponseEntity<List<AdminFeeDtos.PaidFeeOption>> paidFees(@PathVariable Long studentId) {

        return ResponseEntity.ok(feeService.getPaidFees(AdminSecurityContext.requireSchoolId(), studentId));

    }



    @PostMapping("/collect")

    public ResponseEntity<AdminFeeDtos.CollectFeeResponse> collect(

            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,

            @Valid @RequestBody AdminFeeRequests.CollectFeeRequest request) {

        AdminSecurityContext.stamp(request);

        return ResponseEntity.status(HttpStatus.CREATED)

                .body(feeService.collectFee(request, idempotencyKey));

    }



    @PostMapping("/refund")

    public ResponseEntity<AdminFeeDtos.RefundResponse> refund(

            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,

            @Valid @RequestBody AdminFeeRequests.RefundFeeRequest request) {

        AdminSecurityContext.stamp(request);

        return ResponseEntity.status(HttpStatus.CREATED)

                .body(feeService.refundFee(request, idempotencyKey));

    }



    // ─── Student detail + receipt ───────────────────────────────────────────────



    @GetMapping("/students/{studentId}")

    public ResponseEntity<AdminFeeDtos.StudentFeeDetailResponse> studentDetail(@PathVariable Long studentId) {

        return ResponseEntity.ok(feeService.getStudentDetail(AdminSecurityContext.requireSchoolId(), studentId));

    }



    @GetMapping("/transactions/{transactionId}/receipt")

    public ResponseEntity<AdminFeeDtos.ReceiptResponse> receipt(@PathVariable Long transactionId) {

        return ResponseEntity.ok(feeService.getReceipt(AdminSecurityContext.requireSchoolId(), transactionId));

    }



    @GetMapping("/transactions/{transactionId}/receipt/pdf")

    public ResponseEntity<byte[]> receiptPdf(@PathVariable Long transactionId) {

        byte[] pdf = feeService.getReceiptPdf(AdminSecurityContext.requireSchoolId(), transactionId);

        return ResponseEntity.ok()

                .header(HttpHeaders.CONTENT_DISPOSITION,

                        "attachment; filename=receipt_" + transactionId + ".pdf")

                .contentType(MediaType.APPLICATION_PDF)

                .body(pdf);

    }



    @PostMapping("/students/{studentId}/remind")

    public ResponseEntity<AdminFeeDtos.ActionResponse> remind(@PathVariable Long studentId) {

        Long schoolId = AdminSecurityContext.requireSchoolId();

        return ResponseEntity.ok(feeService.remind(schoolId, studentId, AdminSecurityContext.performerName()));

    }



    // ─── Export ─────────────────────────────────────────────────────────────────



    @PostMapping("/export")

    public ResponseEntity<byte[]> export(@Valid @RequestBody AdminFeeRequests.ExportFeeReportRequest request) {

        AdminSecurityContext.stamp(request);

        byte[] body = feeService.exportReport(request);

        return ResponseEntity.ok()

                .header(HttpHeaders.CONTENT_DISPOSITION,

                        "attachment; filename=" + feeService.exportFilename(request.getFormat()))

                .contentType(MediaType.parseMediaType(feeService.exportContentType(request.getFormat())))

                .body(body);

    }



    // ─── Async export (for large reports) ────────────────────────────────────────



    @PostMapping("/export/async")

    public ResponseEntity<AdminFeeDtos.JobStatusResponse> exportAsync(

            @Valid @RequestBody AdminFeeRequests.ExportFeeReportRequest request) {

        AdminSecurityContext.stamp(request);

        return ResponseEntity.accepted().body(feeJobService.enqueueExport(request));

    }



    @GetMapping("/jobs/{jobId}")

    public ResponseEntity<AdminFeeDtos.JobStatusResponse> jobStatus(@PathVariable Long jobId) {

        return ResponseEntity.ok(feeJobService.getStatus(AdminSecurityContext.requireSchoolId(), jobId));

    }



    @GetMapping("/jobs/{jobId}/download")

    public ResponseEntity<byte[]> jobDownload(@PathVariable Long jobId) {

        Long schoolId = AdminSecurityContext.requireSchoolId();

        FeeJob job = feeJobService.getDownloadable(schoolId, jobId);

        return ResponseEntity.ok()

                .header(HttpHeaders.CONTENT_DISPOSITION,

                        "attachment; filename=" + (job.getResultFilename() != null

                                ? job.getResultFilename() : "export"))

                .contentType(MediaType.parseMediaType(job.getResultContentType() != null

                        ? job.getResultContentType()

                        : MediaType.APPLICATION_OCTET_STREAM_VALUE))

                .body(job.getResultData());

    }

}

