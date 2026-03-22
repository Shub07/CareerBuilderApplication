package com.org.careerbuilder.service;

import java.time.LocalDate;

public interface LeaveMetricsService {
    int getUpcomingLeaveCount(Long studentId, LocalDate today);
    String getNextLeaveText(Long studentId, LocalDate today);
}
