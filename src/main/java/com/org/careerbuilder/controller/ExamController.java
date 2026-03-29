package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.security.UserPrincipal;
import com.org.careerbuilder.service.ExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/student/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    /**
     * 📌 Upcoming Exams Tab
     * GET /api/student/exams/upcoming
     */
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingExams(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Long studentId
    ) {
        try {
            log.info("Fetching upcoming exams for student");
            Long resolvedStudentId = (user != null) 
                ? user.getStudentId() 
                : (studentId != null ? studentId : 1L);
            
            log.debug("Resolved student ID: {}", resolvedStudentId);
            List<UpcomingExamResponse> exams = examService.getUpcomingExams(resolvedStudentId);
            log.info("Successfully fetched {} upcoming exams", exams.size());
            return ResponseEntity.ok(exams);
        } catch (Exception e) {
            log.error("Error fetching upcoming exams: {}", e.getMessage(), e);
            return buildErrorResponse("Failed to fetch upcoming exams: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 📌 Completed Exams Tab
     * GET /api/student/exams/completed
     */
    @GetMapping("/completed")
    public ResponseEntity<?> getCompletedExams(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Long studentId
    ) {
        try {
            log.info("Fetching completed exams for student");
            Long resolvedStudentId = (user != null) 
                ? user.getStudentId() 
                : (studentId != null ? studentId : 1L);
            
            log.debug("Resolved student ID: {}", resolvedStudentId);
            List<CompletedExamResponse> exams = examService.getCompletedExams(resolvedStudentId);
            log.info("Successfully fetched {} completed exams", exams.size());
            return ResponseEntity.ok(exams);
        } catch (Exception e) {
            log.error("Error fetching completed exams: {}", e.getMessage(), e);
            return buildErrorResponse("Failed to fetch completed exams: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 📌 View Result - Exam Detail with Breakdown
     * GET /api/student/exams/result/{examResultId}
     */
    @GetMapping("/result/{examResultId}")
    public ResponseEntity<?> getExamResultDetail(
            @PathVariable Long examResultId,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Long studentId
    ) {
        try {
            log.info("Fetching exam result detail for examResult: {}", examResultId);
            Long resolvedStudentId = (user != null) 
                ? user.getStudentId() 
                : (studentId != null ? studentId : 1L);
            
            log.debug("Resolved student ID: {}", resolvedStudentId);
            ExamResultDetailResponse detail = examService.getExamResultDetail(examResultId, resolvedStudentId);
            log.info("Successfully fetched exam result detail");
            return ResponseEntity.ok(detail);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid argument: {}", e.getMessage());
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            log.error("Runtime error: {}", e.getMessage(), e);
            if (e.getMessage().contains("not found")) {
                return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
            } else if (e.getMessage().contains("Unauthorized")) {
                return buildErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
            }
            return buildErrorResponse("Failed to fetch exam result detail: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            return buildErrorResponse("Unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Helper method to build error response
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", message);
        errorResponse.put("status", status.value());
        errorResponse.put("timestamp", System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, status);
    }
}