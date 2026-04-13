package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.response.CertificateGroupResponse;
import com.org.careerbuilder.security.UserPrincipal;
import com.org.careerbuilder.service.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/student/certificates")
@RequiredArgsConstructor
public class CertificatesController {

    private final CertificateService certificateService;

    /**
     * 📌 Certificates List (grouped by category)
     * GET /api/student/certificates
     * Query params:
     *   - academicYear (optional) e.g. "2025-2026"
     *   - search       (optional) search by certificate name
     *   - studentId    (optional, falls back to authenticated user)
     */
    @GetMapping
    public ResponseEntity<?> getCertificates(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String search
    ) {
        try {
            Long resolvedStudentId = resolveStudentId(user, studentId);
            log.info("Fetching certificates for student: {}", resolvedStudentId);

            CertificateGroupResponse response = certificateService.getCertificates(
                    resolvedStudentId, academicYear, search);

            log.info("Fetched {} academic and {} miscellaneous certificates",
                    response.academicCertificates().size(),
                    response.miscellaneousCertificates().size());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Bad request: {}", e.getMessage());
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error fetching certificates: {}", e.getMessage(), e);
            return buildErrorResponse("Failed to fetch certificates: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 📌 Download Certificate
     * GET /api/student/certificates/{certificateId}/download
     */
    @GetMapping("/{certificateId}/download")
    public ResponseEntity<?> downloadCertificate(
            @PathVariable Long certificateId,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Long studentId
    ) {
        try {
            Long resolvedStudentId = resolveStudentId(user, studentId);
            log.info("Downloading certificate {} for student {}", certificateId, resolvedStudentId);

            Resource resource = certificateService.downloadCertificate(certificateId, resolvedStudentId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"certificate_" + certificateId + ".pdf\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            log.warn("Bad request for certificate download: {}", e.getMessage());
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            log.warn("Certificate not available: {}", e.getMessage());
            return buildErrorResponse(e.getMessage(), HttpStatus.CONFLICT);
        } catch (RuntimeException e) {
            log.error("Runtime error downloading certificate: {}", e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
            }
            return buildErrorResponse("Failed to download certificate: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error downloading certificate: {}", e.getMessage(), e);
            return buildErrorResponse("Unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Long resolveStudentId(UserPrincipal user, Long studentId) {
        if (user != null && user.getStudentId() != null) {
            return user.getStudentId();
        }
        if (studentId != null) {
            return studentId;
        }
        throw new IllegalArgumentException("studentId is required");
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> error = new HashMap<>();
        error.put("message", message);
        error.put("status", status.value());
        error.put("timestamp", System.currentTimeMillis());
        return new ResponseEntity<>(error, status);
    }
}
