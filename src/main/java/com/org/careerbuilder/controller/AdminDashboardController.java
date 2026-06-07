package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.response.AdminDashboardDtos;
import com.org.careerbuilder.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH,
                RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * Main admin dashboard — stats, greeting, pending approvals, activities, notice board.
     * GET /api/admin/dashboard/{schoolId}?academicYear=2025-2026
     */
    @GetMapping("/{schoolId}")
    public ResponseEntity<AdminDashboardDtos.AdminDashboardResponse> getDashboard(
            @PathVariable Long schoolId,
            @RequestParam(required = false) String academicYear) {
        return ResponseEntity.ok(adminDashboardService.getDashboard(schoolId, academicYear));
    }
}
