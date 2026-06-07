package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.AdminDashboardDtos;

public interface AdminDashboardService {

    AdminDashboardDtos.AdminDashboardResponse getDashboard(Long schoolId, String academicYear);
}
