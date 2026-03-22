package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.DashboardResponse;

import java.time.LocalDate;

public interface StudentDashboardService {
    DashboardResponse getDashboard(Long studentId, LocalDate date);
}
