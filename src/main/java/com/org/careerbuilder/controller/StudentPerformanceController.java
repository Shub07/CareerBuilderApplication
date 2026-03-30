package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.response.PerformanceReportResponse;
import com.org.careerbuilder.security.UserPrincipal;
import com.org.careerbuilder.service.PerformanceReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/performance")
@RequiredArgsConstructor
public class StudentPerformanceController {

    private final PerformanceReportService performanceReportService;

    /**
     * 🎯 Get Performance Report (All Exams / Internal / Weekly etc.)
     *
     * Examples:
     * GET /api/student/performance/report?examType=INTERNAL
     * GET /api/student/performance/report?examType=WEEKLY
     * GET /api/student/performance/report?examType=FINAL
     * GET /api/student/performance/report (all exams)
     * 
     * For testing without authentication:
     * GET /api/student/performance/report?studentId=1&examType=INTERNAL
     */
    @GetMapping(path = "/report")
    public PerformanceReportResponse getPerformanceReport(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String examType
    ) {
        // Determine studentId: from authentication principal or request parameter
        Long resolvedStudentId = (userPrincipal != null) 
            ? userPrincipal.getStudentId() 
            : (studentId != null ? studentId : 1L); // Default to 1 for testing
        
        return performanceReportService.getPerformanceReport(resolvedStudentId, examType);
    }
}