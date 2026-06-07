package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.TeacherSelfAttendanceDtos;
import com.org.careerbuilder.models.enums.TeacherLeaveStatus;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface TeacherSelfAttendanceService {
    TeacherSelfAttendanceDtos.DashboardResponse getDashboard(Long facultyId);

    List<TeacherSelfAttendanceDtos.AttendanceHistoryRow> getAttendanceHistory(
            Long facultyId, LocalDate fromDate, LocalDate toDate, String status);

    TeacherSelfAttendanceDtos.TodayStatusCard checkIn(Long facultyId, TeacherSelfAttendanceDtos.CheckInRequest request);

    TeacherSelfAttendanceDtos.TodayStatusCard checkOut(Long facultyId, TeacherSelfAttendanceDtos.CheckOutRequest request);

    void applyLeave(Long facultyId, TeacherSelfAttendanceDtos.LeaveApplyRequest request, MultipartFile document);

    List<TeacherSelfAttendanceDtos.LeaveHistoryItem> getLeaveHistory(Long facultyId, TeacherLeaveStatus status);

    List<TeacherSelfAttendanceDtos.SubstituteTeacherOption> listSubstituteTeachers(Long facultyId);

    Resource downloadLeaveDocument(Long facultyId, Long leaveId);

    List<TeacherSelfAttendanceDtos.AdminLeaveItem> getAdminLeaveRequests(Long schoolId, TeacherLeaveStatus status);

    TeacherSelfAttendanceDtos.AdminLeaveItem updateLeaveStatus(Long leaveId, TeacherSelfAttendanceDtos.LeaveActionRequest request);
}