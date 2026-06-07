package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.response.AdminDashboardDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.Notice;
import com.org.careerbuilder.models.Vacation;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.AdminApprovalService;
import com.org.careerbuilder.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final FeeRepository feeRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AdmissionEnquiryRepository admissionEnquiryRepository;
    private final NoticeRepository noticeRepository;
    private final VacationRepository vacationRepository;
    private final AdminActivityLogRepository adminActivityLogRepository;
    private final AdminApprovalService adminApprovalService;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardDtos.AdminDashboardResponse getDashboard(Long schoolId, String academicYear) {
        if (!schoolRepository.existsById(schoolId)) {
            throw new ResourceNotFoundException("School not found: " + schoolId);
        }

        String year = academicYear != null && !academicYear.isBlank()
                ? academicYear
                : currentAcademicYear();

        long totalStudents = studentRepository.countBySchool_Id(schoolId);
        long totalTeachers = facultyRepository.countBySchool_Id(schoolId);
        long classCount = studentRepository.findDistinctClassSections(schoolId).size();

        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        long newAdmissionsMonth = studentProfileRepository
                .countByStudent_School_IdAndAdmissionDateGreaterThanEqual(schoolId, monthStart);
        long newEnquiriesWeek = admissionEnquiryRepository.countBySchool_IdAndCreatedAtGreaterThanEqual(
                schoolId, LocalDateTime.now().minusDays(7));

        BigDecimal pendingFees = feeRepository.sumPendingAmountBySchool(schoolId);
        if (pendingFees == null) {
            pendingFees = BigDecimal.ZERO;
        }

        LocalDateTime since24h = LocalDateTime.now().minusHours(24);
        long newNotices = noticeRepository.countNewNotices(schoolId, since24h);

        var pending = adminApprovalService.listPending(schoolId);
        int pendingCount = pending.items().size();

        AdminDashboardDtos.GreetingHeader header = new AdminDashboardDtos.GreetingHeader(
                buildGreeting(),
                pendingCount + " pending approvals and " + newNotices + " new notices",
                LocalDate.now(),
                year,
                pendingCount,
                (int) newNotices
        );

        List<AdminDashboardDtos.StatCard> stats = List.of(
                new AdminDashboardDtos.StatCard("students", "Total Students", formatCount(totalStudents),
                        "Enrolled", "+" + newAdmissionsMonth + " this month"),
                new AdminDashboardDtos.StatCard("teachers", "Total Teachers", formatCount(totalTeachers),
                        "Active Staff", null),
                new AdminDashboardDtos.StatCard("classes", "Total Classes", formatCount(classCount),
                        classCount > 0 ? "Sections tracked" : "No sections yet", null),
                new AdminDashboardDtos.StatCard("fees", "Pending Fees", "₹ " + formatIndianAmount(pendingFees),
                        "Outstanding", null),
                new AdminDashboardDtos.StatCard("admissions", "New Admissions", formatCount(newEnquiriesWeek),
                        "Enquiries", "+" + (newEnquiriesWeek > 0 ? "active week" : "0 this week"))
        );

        List<AdminDashboardDtos.QuickAction> quickActions = List.of(
                new AdminDashboardDtos.QuickAction("register-student", "Register Student", "/students/register"),
                new AdminDashboardDtos.QuickAction("create-notice", "Create Notice", "/notices/create"),
                new AdminDashboardDtos.QuickAction("add-teacher", "Add Teacher", "/teachers/add"),
                new AdminDashboardDtos.QuickAction("create-exam", "Create Exam", "/exams/create"),
                new AdminDashboardDtos.QuickAction("add-holiday", "Add Holiday", "/holidays/add"),
                new AdminDashboardDtos.QuickAction("new-admission", "New Admission", "/admissions/enquiry")
        );

        List<AdminDashboardDtos.ActivityRow> activities = adminActivityLogRepository
                .findBySchool_IdOrderByCreatedAtDesc(schoolId, PageRequest.of(0, 10))
                .stream()
                .map(log -> new AdminDashboardDtos.ActivityRow(
                        log.getId(),
                        log.getActivityType().name(),
                        log.getTitle(),
                        log.getDescription(),
                        log.getCreatedAt().atZone(ZONE).toInstant()))
                .toList();

        List<AdminDashboardDtos.NoticeBoardItem> board = buildNoticeBoard(schoolId);

        return new AdminDashboardDtos.AdminDashboardResponse(
                header,
                stats,
                quickActions,
                pending.items(),
                activities,
                board
        );
    }

    private List<AdminDashboardDtos.NoticeBoardItem> buildNoticeBoard(Long schoolId) {
        List<AdminDashboardDtos.NoticeBoardItem> items = new ArrayList<>();

        for (Notice n : noticeRepository.findTop10BySchoolIdOrderByCreatedAtDesc(schoolId)) {
            items.add(new AdminDashboardDtos.NoticeBoardItem(
                    n.getId(),
                    n.getTitle(),
                    n.getCategory() != null ? n.getCategory().name() : "GENERAL",
                    n.getCategory() != null ? n.getCategory().getLabel() : "General",
                    n.getCreatedAt() != null ? n.getCreatedAt().toLocalDate() : LocalDate.now(),
                    n.getDescription()
            ));
        }

        for (Vacation v : vacationRepository.findUpcomingVacations(schoolId)) {
            if (items.size() >= 12) {
                break;
            }
            items.add(new AdminDashboardDtos.NoticeBoardItem(
                    v.getId(),
                    v.getVacationName(),
                    v.getVacationType() != null ? v.getVacationType().name() : "HOLIDAY",
                    v.getVacationType() != null ? v.getVacationType().getLabel() : "Holiday",
                    v.getStartDate(),
                    v.getDescription()
            ));
        }

        return items.stream().limit(12).collect(Collectors.toList());
    }

    private String buildGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) {
            return "Good Morning, Admin!";
        }
        if (hour < 17) {
            return "Good Afternoon, Admin!";
        }
        return "Good Evening, Admin!";
    }

    private String currentAcademicYear() {
        int y = LocalDate.now().getYear();
        int m = LocalDate.now().getMonthValue();
        if (m >= 4) {
            return y + "-" + (y + 1);
        }
        return (y - 1) + "-" + y;
    }

    private String formatCount(long n) {
        return String.format("%,d", n);
    }

    private String formatIndianAmount(BigDecimal amount) {
        return amount.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }
}
