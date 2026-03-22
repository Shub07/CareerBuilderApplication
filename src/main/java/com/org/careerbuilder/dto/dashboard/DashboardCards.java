package com.org.careerbuilder.dto.dashboard;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardCards {
    private int attendancePercent;
    private String attendanceDeltaText;

    private int upcomingExamsCount;
    private String nextExamText;

    private int upcomingLeaveCount;
    private String nextLeaveText;

    private int recentNoticesCount;
    private int recentNoticesUnreadCount;

    private int todaysClassesCount;
    private int todaysClassesCompletedCount;

    private int assignmentsCount;
    private String assignmentsDueText;

}
