package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.AttendanceRequest;
import com.org.careerbuilder.dto.request.BulkAttendanceRequest;
import com.org.careerbuilder.dto.response.AttendanceRecordResponse;
import com.org.careerbuilder.dto.response.AttendanceSummaryResponse;
import com.org.careerbuilder.dto.response.ClassAttendanceReportResponse;
import com.org.careerbuilder.dto.response.BulkAttendanceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    // Individual attendance operations
    AttendanceRecordResponse markAttendance(AttendanceRequest request);

    AttendanceRecordResponse updateAttendance(Long attendanceId, AttendanceRequest request);

    AttendanceRecordResponse getAttendanceById(Long attendanceId);

    void deleteAttendance(Long attendanceId);

    // Bulk operations
    BulkAttendanceResponse bulkMarkAttendance(BulkAttendanceRequest request);

    // Student attendance retrieval
    Page<AttendanceRecordResponse> getStudentAttendance(Long studentId, LocalDate from, LocalDate to, Pageable pageable);

    AttendanceSummaryResponse getStudentAttendanceSummary(Long studentId, LocalDate from, LocalDate to);

    // Class attendance operations
    Page<AttendanceRecordResponse> getClassAttendance(String className, String section, LocalDate from, LocalDate to, Pageable pageable);

    ClassAttendanceReportResponse getClassAttendanceReport(String className, String section, LocalDate date);

    List<ClassAttendanceReportResponse> getSchoolAttendanceReport(Long schoolId, LocalDate date);

    // Statistics and analytics
    int getAttendancePercentage(Long studentId, LocalDate from, LocalDate to);

    long getTotalAbsent(Long studentId, LocalDate from, LocalDate to);

    long getTotalPresent(Long studentId, LocalDate from, LocalDate to);

    long getTotalLeave(Long studentId, LocalDate from, LocalDate to);
}

