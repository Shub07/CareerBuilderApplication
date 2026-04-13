package com.org.careerbuilder.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CareerBuilderSummaryResponse {

    private int totalAssignments;
    private int pendingCount;
    private int completedCount;
    private int reportsAvailable;
    private int avgScore;

    private List<CareerAssessmentResponse> upcomingAssessments;
    private List<CareerAssessmentResponse> pendingAssessments;
    private List<CareerAssessmentResponse> inProgressAssessments;
    private List<CareerAssessmentResponse> completedAssessments;
    private List<CareerReportResponse> recentReports;
    private List<CareerPerformanceTrendPointResponse> performanceTrend;
}
