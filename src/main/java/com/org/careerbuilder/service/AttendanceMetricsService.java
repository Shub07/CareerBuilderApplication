package com.org.careerbuilder.service;

import java.time.LocalDate;

public interface AttendanceMetricsService {
    int getAttendancePercent(Long studentId, LocalDate from, LocalDate to);
    String getAttendanceDeltaText(Long studentId, LocalDate from, LocalDate to);
}
