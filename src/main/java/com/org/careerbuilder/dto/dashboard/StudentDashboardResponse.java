package com.org.careerbuilder.dto.dashboard;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class StudentDashboardResponse {
    private StudentHeader student;
    private LocalDate date;
    private DashboardCards cards;
    private RecentExamScore recentExamScore;
    private List<TodayScheduleItem> todaysSchedule;
    private List<NoticeItem> notices;
}
