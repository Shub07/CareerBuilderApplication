package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.PerformanceReportResponse;

/**
 * 🎯 Service for Student Performance Report (LMS Dashboard)
 *
 * Responsibilities:
 * - Aggregate subject-wise performance
 * - Calculate average score
 * - Determine grade & trend
 * - Support exam type filters (ALL / INTERNAL / WEEKLY / FINAL)
 */
public interface PerformanceReportService {


    /**
     * 📊 Get complete performance report of a student
     *
     * @param studentId Logged-in student ID (from JWT)
     * @param examType  Optional filter:
     *                  INTERNAL / WEEKLY / FINAL / null (All Exams)
     *
     * @return PerformanceReportResponse
     */
    PerformanceReportResponse getPerformanceReport(Long studentId, String examType);
}