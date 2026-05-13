package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.TeacherSelfAttendanceDtos;
import com.org.careerbuilder.models.enums.TeacherLeaveStatus;

import java.util.List;

public interface TeacherSelfAttendanceService {
    TeacherSelfAttendanceDtos.DashboardResponse getDashboard(Long facultyId);

    TeacherSelfAttendanceDtos.TodayStatusCard checkIn(Long facultyId, TeacherSelfAttendanceDtos.CheckInRequest request);

    TeacherSelfAttendanceDtos.TodayStatusCard checkOut(Long facultyId, TeacherSelfAttendanceDtos.CheckOutRequest request);

    void applyLeave(Long facultyId, TeacherSelfAttendanceDtos.LeaveApplyRequest request);

    List<TeacherSelfAttendanceDtos.LeaveHistoryItem> getLeaveHistory(Long facultyId, TeacherLeaveStatus status);

    List<TeacherSelfAttendanceDtos.AdminLeaveItem> getAdminLeaveRequests(Long schoolId, TeacherLeaveStatus status);

    TeacherSelfAttendanceDtos.AdminLeaveItem updateLeaveStatus(Long leaveId, TeacherSelfAttendanceDtos.LeaveActionRequest request);
}
