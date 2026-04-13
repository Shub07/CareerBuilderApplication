package com.org.careerbuilder.dto.response;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyLifeDashboardResponse {
    private Long studentId;
    private String studentName;
    private LocalDate selectedDate;
    private Integer totalActivities;
    private Integer completedActivities;
    private Integer completionRate;
    private List<DailyLifeSummaryCardResponse> summaryCards;
    private List<DailyLifeActivityResponse> activities;
    private List<DailyLifeTimelineBlockResponse> timeline;
    private List<DailyLifeQuickCheckItemResponse> quickChecks;
    private List<DailyLifeNotificationResponse> notifications;
}