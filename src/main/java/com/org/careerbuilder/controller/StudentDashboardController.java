package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.DashboardRequest;
import com.org.careerbuilder.dto.response.DashboardResponse;
import com.org.careerbuilder.security.UserPrincipal;
import com.org.careerbuilder.service.StudentDashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class StudentDashboardController {

    private final StudentDashboardService studentDashboardService;

    @PostMapping
    public DashboardResponse getDashboard(@Valid @RequestBody DashboardRequest request) {
        return studentDashboardService.getDashboard(request.studentId(), request.date());
    }

    @GetMapping
    public DashboardResponse getDashboard(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // studentId is trusted because it comes from JWT (not user input)
        return studentDashboardService.getDashboard(principal.getStudentId(), date);
    }
}
