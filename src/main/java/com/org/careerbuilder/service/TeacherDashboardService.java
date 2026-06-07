package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.TeacherDashboardDtos;

public interface TeacherDashboardService {

    TeacherDashboardDtos.TeacherDashboardResponse getDashboard(Long facultyId);
}
