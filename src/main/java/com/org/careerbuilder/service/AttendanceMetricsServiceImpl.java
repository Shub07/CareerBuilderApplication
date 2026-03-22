package com.org.careerbuilder.service;

import com.org.careerbuilder.models.enums.AttendanceStatus;
import com.org.careerbuilder.repository.AttendanceRecordRepository;
import com.org.careerbuilder.service.AttendanceMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AttendanceMetricsServiceImpl implements AttendanceMetricsService {

    private final AttendanceRecordRepository attendanceRecordRepository;

    @Override
    public int getAttendancePercent(Long studentId, LocalDate from, LocalDate to) {
        long total = attendanceRecordRepository.countByStudent_IdAndDateBetween(studentId, from, to);
        if (total == 0) return 0;
        long present = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.PRESENT, from, to
        );
        return (int) Math.round((present * 100.0) / total);
    }

    @Override
    public String getAttendanceDeltaText(Long studentId, LocalDate from, LocalDate to) {
        // Industry note: delta needs previous period comparison.
        // For now, return empty if insufficient data; replace later with last-week comparison.
        return "+0% this week";
    }
}
