package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.DashboardResponse;

import java.util.List;

public interface ExamMetricsService {
    DashboardResponse.RecentExamScore getRecentExamScore(Long studentId);
    int getUpcomingExamsCount(String className, String section); // placeholder (you can connect later)
    String getNextExamText(String className, String section);    // placeholder (you can connect later)
}
