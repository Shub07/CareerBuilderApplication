package com.org.careerbuilder.dto.response;

import java.util.List;

/**
 * Main response for Student Performance Report screen
 */
public record PerformanceReportResponse(

        /**
         * Overall grade (A, B+, B, etc.)
         */
        String overallGrade,

        /**
         * Average percentage score across all subjects
         */
        Double averageScore,

        /**
         * Performance trend text (e.g., "+5% from last month")
         */
        String performanceTrend,

        /**
         * Best performing subject name
         */
        String bestSubject,

        /**
         * Subject-wise detailed performance
         */
        List<SubjectPerformanceResponse> subjects

) {}