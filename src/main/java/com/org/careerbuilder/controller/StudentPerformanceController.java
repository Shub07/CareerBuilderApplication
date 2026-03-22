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
     * Example:
     * GET /api/student/performance?examType=INTERNAL
     */
    @GetMapping(path = "/report")
    public PerformanceReportResponse getPerformanceReport(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String examType
    ) {
        Long studentId = userPrincipal.getStudentId();
        return performanceReportService.getPerformanceReport(studentId, examType);
    }
}