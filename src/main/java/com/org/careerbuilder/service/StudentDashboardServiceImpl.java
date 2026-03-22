package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.DashboardResponse;
import com.org.careerbuilder.models.ClassSession;
import com.org.careerbuilder.models.Notice;
import com.org.careerbuilder.models.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentDashboardServiceImpl implements StudentDashboardService {

    private final StudentLookupService studentLookupService;
    private final AttendanceMetricsService attendanceMetricsService;
    private final ClassScheduleService classScheduleService;
    private final NoticeService noticeService;
    private final AssignmentMetricsService assignmentMetricsService;
    private final LeaveMetricsService leaveMetricsService;
    private final ExamMetricsService examMetricsService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd, MMMM, yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    @Override
    public DashboardResponse getDashboard(Long studentId, LocalDate date) {
        LocalDate today = (date == null) ? LocalDate.now() : date;

        Student student = studentLookupService.getStudentOrThrow(studentId);

        LocalDate weekStart = today.minusDays((today.getDayOfWeek().getValue() % 7) - 1L);
        LocalDate weekEnd = weekStart.plusDays(6);

        int attendancePercent = attendanceMetricsService.getAttendancePercent(studentId, weekStart, weekEnd);
        String attendanceDelta = attendanceMetricsService.getAttendanceDeltaText(studentId, weekStart, weekEnd);

        List<ClassSession> sessions = classScheduleService.getTodaySessions(student.getClassName(), student.getSection(), today);
        int totalClasses = classScheduleService.countTodayClasses(student.getClassName(), student.getSection(), today);
        int completedClasses = classScheduleService.countTodayCompletedClasses(student.getClassName(), student.getSection(), today);

        List<DashboardResponse.ScheduleItem> scheduleItems = sessions.stream()
                .filter(s -> s.getSubject() != null && s.getTeacher() != null)
                .map(s -> new DashboardResponse.ScheduleItem(
                        0,
                        s.getSubject().getName(),
                        s.getTeacher().getName(),
                        s.getStartTime().format(TIME_FMT) + " - " + s.getEndTime().format(TIME_FMT)
                ))
                .toList();

        List<Notice> recentNotices = noticeService.getRecentNotices(student.getSchoolId());
        int noticesTotal = noticeService.getTotalNoticeCount(student.getSchoolId());
        int noticesUnread = noticeService.getUnreadNoticeCount(studentId, recentNotices);

        List<DashboardResponse.NoticeItem> noticeItems = recentNotices.stream()
                .map(n -> new DashboardResponse.NoticeItem(
                        n.getTitle(),
                        n.getCreatedAt().toLocalDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                        true // If you want per-item accurate unread, move logic into NoticeService (recommended)
                ))
                .toList();

        int assignmentsTotal = assignmentMetricsService.getTotalAssignments(studentId);
        int dueThisWeek = assignmentMetricsService.getDueThisWeek(studentId, weekStart, weekEnd);

        int upcomingLeaveCount = leaveMetricsService.getUpcomingLeaveCount(studentId, today);
        String nextLeaveText = leaveMetricsService.getNextLeaveText(studentId, today);

        int upcomingExamsCount = examMetricsService.getUpcomingExamsCount(student.getClassName(), student.getSection());
        String nextExamText = examMetricsService.getNextExamText(student.getClassName(), student.getSection());
        DashboardResponse.RecentExamScore recentExamScore = examMetricsService.getRecentExamScore(studentId);

        DashboardResponse.Header header = new DashboardResponse.Header(
                "Good Morning " + student.getFirstName() + ",",
                today.format(DATE_FMT)
        );

        DashboardResponse.DashboardCards cards = new DashboardResponse.DashboardCards(
                attendancePercent,
                attendanceDelta,
                upcomingExamsCount,
                nextExamText,
                upcomingLeaveCount,
                nextLeaveText,
                noticesTotal,
                noticesUnread,
                totalClasses,
                completedClasses,
                assignmentsTotal,
                dueThisWeek + " due this week"
        );

        return new DashboardResponse(
                header,
                cards,
                recentExamScore,
                new DashboardResponse.TodaySchedule("Today's Schedule", scheduleItems),
                new DashboardResponse.NoticePanel("Notices", noticeItems)
        );
    }
}
