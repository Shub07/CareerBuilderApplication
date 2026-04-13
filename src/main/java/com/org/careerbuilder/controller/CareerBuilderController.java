package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.CareerSaveProgressRequest;
import com.org.careerbuilder.dto.request.CareerSubmitRequest;
import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.service.CareerBuilderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/career-builder")
@RequiredArgsConstructor
public class CareerBuilderController {

    private final CareerBuilderService service;

    // ── Summary ─────────────────────────────────────────────────────────────────

    /**
     * GET /api/career-builder/summary?studentId=&schoolId=
     * Returns stats cards + in-progress/completed lists for the dashboard.
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(
            @RequestParam Long studentId,
            @RequestParam(defaultValue = "1") Long schoolId
    ) {
        try {
            CareerBuilderSummaryResponse data = service.getSummary(studentId, schoolId);
            return ResponseEntity.ok(success("Summary retrieved", data));
        } catch (Exception e) {
            log.error("Error in career-builder summary: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    // ── Assessments list ─────────────────────────────────────────────────────────

    /**
     * GET /api/career-builder/assessments?studentId=&schoolId=
     */
    @GetMapping("/assessments")
    public ResponseEntity<Map<String, Object>> getAssessments(
            @RequestParam Long studentId,
            @RequestParam(defaultValue = "1") Long schoolId
    ) {
        try {
            List<CareerAssessmentResponse> data = service.getAssessments(studentId, schoolId);
            return ResponseEntity.ok(success("Assessments retrieved", data));
        } catch (Exception e) {
            log.error("Error getting assessments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    // ── Questions ────────────────────────────────────────────────────────────────

    /**
     * GET /api/career-builder/assessments/{id}/questions?studentId=
     */
    @GetMapping("/assessments/{id}/questions")
    public ResponseEntity<Map<String, Object>> getQuestions(
            @PathVariable Long id,
            @RequestParam Long studentId
    ) {
        try {
            CareerQuestionsResponse data = service.getQuestions(id, studentId);
            return ResponseEntity.ok(success("Questions retrieved", data));
        } catch (Exception e) {
            log.error("Error getting questions for assessment {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    // ── Start / Resume ───────────────────────────────────────────────────────────

    /**
     * POST /api/career-builder/assessments/{id}/start?studentId=
     */
    @PostMapping("/assessments/{id}/start")
    public ResponseEntity<Map<String, Object>> startOrResume(
            @PathVariable Long id,
            @RequestParam Long studentId
    ) {
        try {
            CareerStudentProgressIdResponse data = service.startOrResume(id, studentId);
            return ResponseEntity.ok(success("Assessment started/resumed", data));
        } catch (Exception e) {
            log.error("Error starting assessment {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    // ── Save progress ────────────────────────────────────────────────────────────

    /**
     * PUT /api/career-builder/assessments/{id}/progress?studentId=
     */
    @PutMapping("/assessments/{id}/progress")
    public ResponseEntity<Map<String, Object>> saveProgress(
            @PathVariable Long id,
            @RequestParam Long studentId,
            @RequestBody CareerSaveProgressRequest request
    ) {
        try {
            service.saveProgress(id, studentId, request);
            return ResponseEntity.ok(success("Progress saved", null));
        } catch (Exception e) {
            log.error("Error saving progress for assessment {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    // ── Submit ───────────────────────────────────────────────────────────────────

    /**
     * POST /api/career-builder/assessments/{id}/submit?studentId=
     */
    @PostMapping("/assessments/{id}/submit")
    public ResponseEntity<Map<String, Object>> submit(
            @PathVariable Long id,
            @RequestParam Long studentId,
            @RequestBody CareerSubmitRequest request
    ) {
        try {
            CareerResultResponse data = service.submit(id, studentId, request);
            return ResponseEntity.ok(success("Assessment submitted successfully", data));
        } catch (Exception e) {
            log.error("Error submitting assessment {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    // ── Result ───────────────────────────────────────────────────────────────────

    /**
     * GET /api/career-builder/assessments/{id}/result?studentId=
     */
    @GetMapping("/assessments/{id}/result")
    public ResponseEntity<Map<String, Object>> getResult(
            @PathVariable Long id,
            @RequestParam Long studentId
    ) {
        try {
            CareerResultResponse data = service.getResult(id, studentId);
            return ResponseEntity.ok(success("Result retrieved", data));
        } catch (Exception e) {
            log.error("Error getting result for assessment {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    // ── Reports ──────────────────────────────────────────────────────────────────

    /**
     * GET /api/career-builder/reports?studentId=
     */
    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getReports(
            @RequestParam Long studentId
    ) {
        try {
            List<CareerReportResponse> data = service.getReports(studentId);
            return ResponseEntity.ok(success("Reports retrieved", data));
        } catch (Exception e) {
            log.error("Error getting reports: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    /**
     * GET /api/career-builder/reports/{resultId}/download?studentId=
     */
    @GetMapping("/reports/{resultId}/download")
    public ResponseEntity<?> downloadReport(
            @PathVariable Long resultId,
            @RequestParam Long studentId
    ) {
        try {
            CareerReportDownloadPayload payload = service.downloadReport(resultId, studentId);
            ByteArrayResource resource = new ByteArrayResource(payload.content());

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + payload.fileName() + "\"")
                    .contentLength(payload.content().length)
                    .body(resource);
        } catch (Exception e) {
            log.error("Error downloading report {}: {}", resultId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────────

    private Map<String, Object> success(String message, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("message", message);
        map.put("data", data);
        return map;
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        map.put("message", message);
        return map;
    }
}
