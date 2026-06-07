package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.AdminQuickActionRequests;
import com.org.careerbuilder.dto.request.TeacherSelfAttendanceDtos;
import com.org.careerbuilder.dto.response.AdminDashboardDtos;
import com.org.careerbuilder.dto.response.AdminOperationResponses;
import com.org.careerbuilder.models.AdmissionEnquiry;
import com.org.careerbuilder.models.Faculty;
import com.org.careerbuilder.models.LeaveRequest;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.models.TeacherLeaveRequest;
import com.org.careerbuilder.models.enums.AdminActivityType;
import com.org.careerbuilder.models.enums.AdminApprovalKind;
import com.org.careerbuilder.models.enums.AdmissionEnquiryStatus;
import com.org.careerbuilder.models.enums.LeaveStatus;
import com.org.careerbuilder.models.enums.TeacherLeaveStatus;
import com.org.careerbuilder.repository.AdmissionEnquiryRepository;
import com.org.careerbuilder.repository.LeaveRequestRepository;
import com.org.careerbuilder.repository.TeacherLeaveRequestRepository;
import com.org.careerbuilder.service.AdminApprovalService;
import com.org.careerbuilder.service.LeaveRequestService;
import com.org.careerbuilder.service.TeacherSelfAttendanceService;
import com.org.careerbuilder.service.support.AdminActivityLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminApprovalServiceImpl implements AdminApprovalService {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final TeacherLeaveRequestRepository teacherLeaveRequestRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AdmissionEnquiryRepository admissionEnquiryRepository;
    private final TeacherSelfAttendanceService teacherSelfAttendanceService;
    private final LeaveRequestService leaveRequestService;
    private final AdminActivityLogger activityLogger;

    @Override
    @Transactional(readOnly = true)
    public AdminOperationResponses.ApprovalListResponse listPending(Long schoolId) {
        List<AdminDashboardDtos.PendingApprovalRow> rows = new ArrayList<>();

        for (TeacherLeaveRequest lr : teacherLeaveRequestRepository
                .findBySchoolIdAndStatusOrderByCreatedAtDesc(schoolId, TeacherLeaveStatus.APPLIED)) {
            Faculty f = lr.getFaculty();
            String name = f.getFirstName() + " " + f.getLastName();
            Instant when = lr.getCreatedAt() != null ? lr.getCreatedAt().atZone(ZONE).toInstant() : Instant.now();
            rows.add(new AdminDashboardDtos.PendingApprovalRow(
                    compositeId(AdminApprovalKind.TEACHER_LEAVE, lr.getId()),
                    AdminApprovalKind.TEACHER_LEAVE.name(),
                    name,
                    "Leave",
                    lr.getLeaveType() + " — " + lr.getFromDate() + " to " + lr.getToDate(),
                    when,
                    true
            ));
        }

        for (LeaveRequest lr : leaveRequestRepository.findBySchoolIdAndStatus(schoolId, LeaveStatus.APPLIED)) {
            Student st = lr.getStudent();
            String name = st.getFirstName() + " " + st.getLastName();
            Instant when = lr.getCreatedAt() != null ? lr.getCreatedAt().atZone(ZONE).toInstant() : Instant.now();
            rows.add(new AdminDashboardDtos.PendingApprovalRow(
                    compositeId(AdminApprovalKind.STUDENT_LEAVE, lr.getId()),
                    AdminApprovalKind.STUDENT_LEAVE.name(),
                    name,
                    "Leave",
                    lr.getReason(),
                    when,
                    true
            ));
        }

        for (AdmissionEnquiry en : admissionEnquiryRepository
                .findBySchool_IdAndStatusOrderByCreatedAtDesc(schoolId, AdmissionEnquiryStatus.PENDING)) {
            Instant when = en.getCreatedAt() != null ? en.getCreatedAt().atZone(ZONE).toInstant() : Instant.now();
            rows.add(new AdminDashboardDtos.PendingApprovalRow(
                    compositeId(AdminApprovalKind.ADMISSION_ENQUIRY, en.getId()),
                    AdminApprovalKind.ADMISSION_ENQUIRY.name(),
                    en.getStudentName(),
                    "Admission Enquiry",
                    "Class " + en.getClassApplying() + " — " + en.getPhone(),
                    when,
                    true
            ));
        }

        rows.sort(Comparator.comparing(AdminDashboardDtos.PendingApprovalRow::requestedAt).reversed());
        return new AdminOperationResponses.ApprovalListResponse(rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminDashboardDtos.PendingApprovalRow decide(String compositeId,
                                                          AdminQuickActionRequests.ApprovalDecisionRequest request) {
        ParsedId parsed = parseCompositeId(compositeId);
        boolean approve = "APPROVE".equalsIgnoreCase(request.getKind())
                || "APPROVED".equalsIgnoreCase(request.getKind());

        return switch (parsed.kind()) {
            case TEACHER_LEAVE -> decideTeacherLeave(parsed.id(), approve, request);
            case STUDENT_LEAVE -> decideStudentLeave(parsed.id(), approve, request);
            case ADMISSION_ENQUIRY -> decideAdmission(parsed.id(), approve, request);
        };
    }

    private AdminDashboardDtos.PendingApprovalRow decideTeacherLeave(Long leaveId, boolean approve,
                                                                     AdminQuickActionRequests.ApprovalDecisionRequest request) {
        TeacherLeaveStatus status = approve ? TeacherLeaveStatus.APPROVED : TeacherLeaveStatus.REJECTED;
        var action = new TeacherSelfAttendanceDtos.LeaveActionRequest(
                status, request.getPerformedBy(), request.getRejectionReason());
        var item = teacherSelfAttendanceService.updateLeaveStatus(leaveId, action);
        TeacherLeaveRequest lr = teacherLeaveRequestRepository.findById(leaveId).orElseThrow();
        Long schoolId = lr.getSchoolId();
        activityLogger.log(schoolId, approve ? AdminActivityType.LEAVE_APPROVED : AdminActivityType.LEAVE_REJECTED,
                "Teacher leave " + (approve ? "approved" : "rejected"),
                item.facultyName(),
                "TEACHER_LEAVE", leaveId, request.getPerformedBy());
        return new AdminDashboardDtos.PendingApprovalRow(
                compositeId(AdminApprovalKind.TEACHER_LEAVE, leaveId),
                AdminApprovalKind.TEACHER_LEAVE.name(),
                item.facultyName(),
                "Leave",
                item.leaveType(),
                Instant.now(),
                false
        );
    }

    private AdminDashboardDtos.PendingApprovalRow decideStudentLeave(Long leaveId, boolean approve,
                                                                     AdminQuickActionRequests.ApprovalDecisionRequest request) {
        LeaveStatus status = approve ? LeaveStatus.APPROVED : LeaveStatus.REJECTED;
        leaveRequestService.updateLeaveStatus(leaveId, status, request.getRejectionReason());
        LeaveRequest lr = leaveRequestRepository.findById(leaveId).orElseThrow();
        Student st = lr.getStudent();
        Long schoolId = st.getSchool().getId();
        activityLogger.log(schoolId, approve ? AdminActivityType.LEAVE_APPROVED : AdminActivityType.LEAVE_REJECTED,
                "Student leave " + (approve ? "approved" : "rejected"),
                st.getFirstName() + " " + st.getLastName(),
                "STUDENT_LEAVE", leaveId, request.getPerformedBy());
        return new AdminDashboardDtos.PendingApprovalRow(
                compositeId(AdminApprovalKind.STUDENT_LEAVE, leaveId),
                AdminApprovalKind.STUDENT_LEAVE.name(),
                st.getFirstName() + " " + st.getLastName(),
                "Leave",
                lr.getReason(),
                Instant.now(),
                false
        );
    }

    private AdminDashboardDtos.PendingApprovalRow decideAdmission(Long enquiryId, boolean approve,
                                                                AdminQuickActionRequests.ApprovalDecisionRequest request) {
        AdmissionEnquiry en = admissionEnquiryRepository.findById(enquiryId).orElseThrow();
        en.setStatus(approve ? AdmissionEnquiryStatus.FOLLOWED_UP : AdmissionEnquiryStatus.CLOSED);
        admissionEnquiryRepository.save(en);
        activityLogger.log(en.getSchool().getId(), AdminActivityType.OTHER,
                "Admission enquiry " + (approve ? "followed up" : "closed"),
                en.getStudentName(),
                "ADMISSION_ENQUIRY", enquiryId, request.getPerformedBy());
        return new AdminDashboardDtos.PendingApprovalRow(
                compositeId(AdminApprovalKind.ADMISSION_ENQUIRY, enquiryId),
                AdminApprovalKind.ADMISSION_ENQUIRY.name(),
                en.getStudentName(),
                "Admission Enquiry",
                en.getClassApplying(),
                Instant.now(),
                false
        );
    }

    private String compositeId(AdminApprovalKind kind, Long id) {
        return kind.name() + ":" + id;
    }

    private ParsedId parseCompositeId(String compositeId) {
        if (compositeId == null || !compositeId.contains(":")) {
            throw new IllegalArgumentException("Invalid approval id format. Use KIND:id e.g. TEACHER_LEAVE:12");
        }
        String[] parts = compositeId.split(":", 2);
        AdminApprovalKind kind = AdminApprovalKind.valueOf(parts[0]);
        return new ParsedId(kind, Long.parseLong(parts[1]));
    }

    private record ParsedId(AdminApprovalKind kind, Long id) {
    }
}
