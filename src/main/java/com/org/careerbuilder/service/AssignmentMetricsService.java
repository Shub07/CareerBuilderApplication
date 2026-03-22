package com.org.careerbuilder.service;

import java.time.LocalDate;

public interface AssignmentMetricsService {
    int getTotalAssignments(Long studentId);

    int getDueThisWeek(Long studentId, LocalDate start, LocalDate end);
}
