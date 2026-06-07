package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.response.TeacherDashboardDtos;
import com.org.careerbuilder.dto.response.TeacherMyClassesDtos;
import com.org.careerbuilder.dto.response.TeacherScheduleDtos;
import com.org.careerbuilder.dto.response.TeacherStudentPerformanceDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.ClassSubjectTeacher;
import com.org.careerbuilder.models.Faculty;
import com.org.careerbuilder.models.TeacherNotice;
import com.org.careerbuilder.models.enums.AssignmentStatus;
import com.org.careerbuilder.models.enums.TeacherNoticeStatus;
import com.org.careerbuilder.repository.AssignmentSubmissionRepository;
import com.org.careerbuilder.repository.ClassSubjectTeacherRepository;
import com.org.careerbuilder.repository.FacultyRepository;
import com.org.careerbuilder.repository.TeacherNoticeRepository;
import com.org.careerbuilder.service.TeacherDashboardService;
import com.org.careerbuilder.service.TeacherMyClassesService;
import com.org.careerbuilder.service.TeacherScheduleService;
import com.org.careerbuilder.service.TeacherStudentPerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherDashboardServiceImpl implements TeacherDashboardService {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final FacultyRepository facultyRepository;
    private final ClassSubjectTeacherRepository classSubjectTeacherRepository;
    private final TeacherScheduleService teacherScheduleService;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final TeacherNoticeRepository teacherNoticeRepository;
    private final TeacherStudentPerformanceService teacherStudentPerformanceService;
    private final TeacherMyClassesService teacherMyClassesService;

    @Override
    public TeacherDashboardDtos.TeacherDashboardResponse getDashboard(Long facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));

        String greeting = buildGreeting(faculty);
        LocalDate today = LocalDate.now();
        TeacherScheduleDtos.DayScheduleResponse day = teacherScheduleService.getDaySchedule(facultyId, today, null, null);

        int teachingPeriods = (int) day.items().stream()
                .filter(it -> !"BREAK".equalsIgnoreCase(String.valueOf(it.activityType())))
                .count();

        List<TeacherDashboardDtos.DashboardPendingActionRow> actions = new ArrayList<>();
        PageRequest top = PageRequest.of(0, 6);
        List<Object[]> gradingBuckets = assignmentSubmissionRepository.aggregatePendingGradingByAssignment(
                facultyId,
                List.of(AssignmentStatus.SUBMITTED, AssignmentStatus.LATE),
                top);
        int g = 0;
        for (Object[] row : gradingBuckets) {
            String title = (String) row[0];
            long cnt = ((Number) row[1]).longValue();
            LocalDateTime latest = (LocalDateTime) row[2];
            Instant when = latest != null ? latest.atZone(ZONE).toInstant() : Instant.now();
            String line = cnt == 1
                    ? "1 submission to grade: " + title
                    : cnt + " submissions to grade: " + title;
            actions.add(new TeacherDashboardDtos.DashboardPendingActionRow(
                    "grading-" + (++g) + "-" + (title != null ? title.hashCode() : 0),
                    line,
                    when,
                    "SUBMISSION"));
        }

        List<TeacherNotice> notices = teacherNoticeRepository.findByFaculty_IdOrderByCreatedAtDesc(facultyId);
        int n = 0;
        for (TeacherNotice notice : notices) {
            if (notice.getStatus() != TeacherNoticeStatus.PUBLISHED) {
                continue;
            }
            if (++n > 4) {
                break;
            }
            LocalDateTime whenLdt = notice.getPublishedAt() != null ? notice.getPublishedAt() : notice.getCreatedAt();
            Instant when = whenLdt != null ? whenLdt.atZone(ZONE).toInstant() : Instant.now();
            String t = notice.getTitle() != null ? notice.getTitle() : "Notice";
            actions.add(new TeacherDashboardDtos.DashboardPendingActionRow(
                    "notice-" + notice.getId(),
                    "Published notice: " + t,
                    when,
                    "NOTICE"));
        }

        actions.sort(Comparator.comparing(TeacherDashboardDtos.DashboardPendingActionRow::occurredAt).reversed());
        List<TeacherDashboardDtos.DashboardPendingActionRow> topActions = actions.stream().limit(8).toList();

        TeacherMyClassesDtos.ClassSessionListResponse todayClasses =
                teacherMyClassesService.listSessions(facultyId, "today", null, null);
        List<TeacherDashboardDtos.TodayClassSessionRow> todayRows = todayClasses.sessions().stream()
                .map(this::mapTodayClassSession)
                .toList();
        int attendancePendingToday = (int) todayRows.stream()
                .filter(TeacherDashboardDtos.TodayClassSessionRow::attendancePending)
                .count();
        if (attendancePendingToday > 0) {
            actions.add(new TeacherDashboardDtos.DashboardPendingActionRow(
                    "classes-attendance-pending",
                    attendancePendingToday == 1
                            ? "1 class session needs attendance marked"
                            : attendancePendingToday + " class sessions need attendance marked",
                    Instant.now(),
                    "ATTENDANCE"));
        }
        actions.sort(Comparator.comparing(TeacherDashboardDtos.DashboardPendingActionRow::occurredAt).reversed());
        topActions = actions.stream().limit(8).toList();

        String summaryLine = buildSummaryLine(teachingPeriods, attendancePendingToday, topActions.size());
        TeacherDashboardDtos.PerformanceSnapshot performanceSnapshot = buildPerformanceSnapshot(facultyId);
        return new TeacherDashboardDtos.TeacherDashboardResponse(
                greeting, summaryLine, teachingPeriods, topActions, performanceSnapshot, todayRows, attendancePendingToday);
    }

    private TeacherDashboardDtos.TodayClassSessionRow mapTodayClassSession(TeacherMyClassesDtos.ClassSessionCard c) {
        return new TeacherDashboardDtos.TodayClassSessionRow(
                c.sessionKind(),
                c.refId(),
                c.sessionDate() != null ? c.sessionDate().toString() : null,
                c.subjectName(),
                c.gradeDisplay(),
                c.className(),
                c.section(),
                c.timeRangeLabel(),
                c.studentCount(),
                c.attendancePending(),
                c.cardStatus(),
                c.lastTopic() != null && !c.lastTopic().isBlank() ? c.lastTopic() : null);
    }

    private TeacherDashboardDtos.PerformanceSnapshot buildPerformanceSnapshot(Long facultyId) {
        List<ClassSubjectTeacher> assignments = classSubjectTeacherRepository.findByFaculty_IdAndActiveTrueWithSubject(facultyId);
        if (assignments.isEmpty()) {
            return null;
        }
        ClassSubjectTeacher first = assignments.get(0);
        if (first.getClassName() == null || first.getSection() == null) {
            return null;
        }
        try {
            TeacherStudentPerformanceDtos.KpiResponse k = teacherStudentPerformanceService.getClassKpis(
                    facultyId, first.getClassName(), first.getSection());
            String label = "Grade " + first.getClassName() + " " + first.getSection();
            return new TeacherDashboardDtos.PerformanceSnapshot(
                    label,
                    first.getClassName(),
                    first.getSection(),
                    k.classAveragePercent(),
                    k.classAverageTrendText(),
                    k.passPercent(),
                    k.passStatusLabel(),
                    (int) k.activeStudents(),
                    k.improvementRatePercent(),
                    k.improvementStatusLabel());
        } catch (Exception ex) {
            return null;
        }
    }

    private static String buildGreeting(Faculty f) {
        String first = f.getFirstName() != null ? f.getFirstName().trim() : "";
        String last = f.getLastName() != null ? f.getLastName().trim() : "";
        String name = (first + " " + last).trim();
        if (name.isEmpty()) {
            name = "there";
        }
        int hour = LocalTime.now().getHour();
        String part = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";
        return part + ", " + name + "!";
    }

    private static String buildSummaryLine(int teachingPeriods, int attendancePending, int pendingCount) {
        String sessionsPart = teachingPeriods > 0
                ? "You have " + teachingPeriods + " class session" + (teachingPeriods == 1 ? "" : "s") + " on your timetable today."
                : "Nothing scheduled on your timetable for today.";
        String attendPart = attendancePending > 0
                ? " " + attendancePending + " need" + (attendancePending == 1 ? "s" : "") + " attendance marked."
                : "";
        if (pendingCount <= 0) {
            return sessionsPart + attendPart;
        }
        String queue = " You have " + pendingCount + " recent item" + (pendingCount == 1 ? "" : "s") + " in your queue.";
        return sessionsPart + attendPart + queue;
    }
}
