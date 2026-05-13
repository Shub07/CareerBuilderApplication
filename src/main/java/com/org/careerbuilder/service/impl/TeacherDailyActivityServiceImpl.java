package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.TeacherDailyActivityReviewRequest;
import com.org.careerbuilder.dto.response.TeacherDailyActivityDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.DailyLifeActivityType;
import com.org.careerbuilder.models.enums.TeacherDailyActivityStatus;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.TeacherDailyActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherDailyActivityServiceImpl implements TeacherDailyActivityService {

    private final FacultyRepository facultyRepository;
    private final ClassSubjectTeacherRepository classSubjectTeacherRepository;
    private final StudentRepository studentRepository;
    private final DailyLifeActivityRepository dailyLifeActivityRepository;
    private final TeacherDailyActivityReviewRepository teacherDailyActivityReviewRepository;

    private Faculty loadFaculty(Long facultyId) {
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherDailyActivityDtos.ClassSectionFilter> getFilters(Long facultyId) {
        loadFaculty(facultyId);
        return classSubjectTeacherRepository.findByFaculty_IdAndActiveTrue(facultyId)
                .stream()
                .map(c -> new TeacherDailyActivityDtos.ClassSectionFilter(c.getClassName(), c.getSection(), c.getClassName() + " " + c.getSection()))
                .distinct()
                .sorted(Comparator.comparing(TeacherDailyActivityDtos.ClassSectionFilter::className)
                        .thenComparing(TeacherDailyActivityDtos.ClassSectionFilter::section))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherDailyActivityDtos.DailyStudentListResponse getStudentList(
            Long facultyId, String className, String section, String range, LocalDate date, String search) {
        Faculty faculty = loadFaculty(facultyId);
        if (className == null || className.isBlank() || section == null || section.isBlank()) {
            throw new IllegalArgumentException("className and section are required");
        }

        validateTeacherAssignedClass(facultyId, className, section);
        LocalDate targetDate = date != null ? date : LocalDate.now();
        LocalDate from = resolveFrom(range, targetDate);
        LocalDate to = targetDate;

        List<Student> students = studentRepository.findBySchool_IdAndClassNameAndSection(
                faculty.getSchool().getId(), className.trim(), section.trim());

        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase(Locale.ROOT);
            students = students.stream()
                    .filter(s -> (s.getFirstName() + " " + s.getLastName()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(s.getRollNo()).contains(q))
                    .toList();
        }

        List<Long> studentIds = students.stream().map(Student::getId).toList();
        List<DailyLifeActivity> dayActivities = studentIds.isEmpty() ? List.of()
                : dailyLifeActivityRepository.findByStudent_IdInAndActivityDateOrderByStudent_IdAscStartTimeAsc(studentIds, targetDate);
        Map<Long, List<DailyLifeActivity>> byStudentDay = dayActivities.stream().collect(Collectors.groupingBy(a -> a.getStudent().getId()));
        Map<Long, TeacherDailyActivityReview> reviewByStudent = teacherDailyActivityReviewRepository
                .findByFaculty_IdAndActivityDateAndStudent_IdIn(facultyId, targetDate, studentIds)
                .stream()
                .collect(Collectors.toMap(r -> r.getStudent().getId(), Function.identity()));

        List<TeacherDailyActivityDtos.StudentActivityRow> rows = new ArrayList<>();
        double sumStudy = 0;
        double sumPhysical = 0;
        double sumRest = 0;
        int loggedCount = 0;

        for (Student s : students) {
            List<DailyLifeActivity> acts = byStudentDay.getOrDefault(s.getId(), List.of());
            if (!acts.isEmpty()) loggedCount++;
            double school = hoursForType(acts, DailyLifeActivityType.SCHOOL_TIME);
            double study = hoursForType(acts, DailyLifeActivityType.STUDY_HOMEWORK);
            double physical = hoursForType(acts, DailyLifeActivityType.PHYSICAL_ACTIVITY);
            double rest = hoursForType(acts, DailyLifeActivityType.REST_TIME);
            sumStudy += study;
            sumPhysical += physical;
            sumRest += rest;

            TeacherDailyActivityStatus computed = computeStatus(acts, reviewByStudent.get(s.getId()));

            rows.add(new TeacherDailyActivityDtos.StudentActivityRow(
                    s.getId(),
                    s.getFirstName() + " " + s.getLastName(),
                    s.getRollNo(),
                    initials(s),
                    round1(school),
                    round1(study),
                    round1(physical),
                    round1(rest),
                    computed.name()
            ));
        }

        rows.sort(Comparator.comparing(TeacherDailyActivityDtos.StudentActivityRow::rollNo, Comparator.nullsLast(Comparator.naturalOrder())));

        int total = rows.size();
        TeacherDailyActivityDtos.ListSummaryCards cards = new TeacherDailyActivityDtos.ListSummaryCards(
                total == 0 ? 0 : round1(sumStudy / total),
                total == 0 ? 0 : round1(sumPhysical / total),
                total == 0 ? 0 : round1(sumRest / total),
                total == 0 ? 0 : (int) Math.round(loggedCount * 100.0 / total)
        );

        return new TeacherDailyActivityDtos.DailyStudentListResponse(targetDate, className.trim(), section.trim(), cards, rows);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherDailyActivityDtos.StudentDetailResponse getStudentDetail(Long facultyId, Long studentId, LocalDate date) {
        Faculty faculty = loadFaculty(facultyId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (!student.getSchool().getId().equals(faculty.getSchool().getId())) {
            throw new IllegalArgumentException("Student not in your school");
        }
        validateTeacherAssignedClass(facultyId, student.getClassName(), student.getSection());

        LocalDate targetDate = date != null ? date : LocalDate.now();
        LocalDate weekFrom = targetDate.minusDays(6);

        List<DailyLifeActivity> dayActs = dailyLifeActivityRepository.findByStudent_IdAndActivityDateOrderByStartTimeAsc(studentId, targetDate);
        List<DailyLifeActivity> weekActs = dailyLifeActivityRepository.findByStudent_IdAndActivityDateBetweenOrderByActivityDateAscStartTimeAsc(
                studentId, weekFrom, targetDate);

        double daySchool = hoursForType(dayActs, DailyLifeActivityType.SCHOOL_TIME);
        double dayStudy = hoursForType(dayActs, DailyLifeActivityType.STUDY_HOMEWORK);
        double dayPhysical = hoursForType(dayActs, DailyLifeActivityType.PHYSICAL_ACTIVITY);
        double dayRest = hoursForType(dayActs, DailyLifeActivityType.REST_TIME);

        TeacherDailyActivityReview review = teacherDailyActivityReviewRepository
                .findByFaculty_IdAndStudent_IdAndActivityDate(facultyId, studentId, targetDate)
                .orElse(null);
        String status = computeStatus(dayActs, review).name();

        List<TeacherDailyActivityDtos.DaySegment> segments = dayActs.stream()
                .map(a -> new TeacherDailyActivityDtos.DaySegment(
                        a.getActivityType().getLabel(),
                        a.getStartTime(),
                        a.getEndTime(),
                        round1(a.getDurationMinutes() / 60.0),
                        a.getActivityType().getColor()))
                .toList();

        List<TeacherDailyActivityDtos.ActivityLogItem> logs = dayActs.stream()
                .map(a -> new TeacherDailyActivityDtos.ActivityLogItem(
                        a.getActivityType().name(),
                        a.getStartTime(),
                        a.getEndTime(),
                        round1(a.getDurationMinutes() / 60.0),
                        a.getNote(),
                        a.getActivityType().getColor()))
                .toList();

        double weeklyStudyAvg = averageDaily(weekActs, DailyLifeActivityType.STUDY_HOMEWORK);
        double weeklyPhysicalAvg = averageDaily(weekActs, DailyLifeActivityType.PHYSICAL_ACTIVITY);
        double weeklyRestAvg = averageDaily(weekActs, DailyLifeActivityType.REST_TIME);

        return new TeacherDailyActivityDtos.StudentDetailResponse(
                student.getId(),
                student.getFirstName() + " " + student.getLastName(),
                student.getRollNo(),
                student.getClassName(),
                student.getSection(),
                targetDate,
                round1(daySchool),
                round1(dayStudy),
                round1(dayPhysical),
                round1(dayRest),
                segments,
                logs,
                new TeacherDailyActivityDtos.WeeklySummary(round1(weeklyStudyAvg), round1(weeklyPhysicalAvg), round1(weeklyRestAvg)),
                status
        );
    }

    @Override
    @Transactional
    public TeacherDailyActivityDtos.ReviewResponse upsertReview(Long facultyId, TeacherDailyActivityReviewRequest request) {
        Faculty faculty = loadFaculty(facultyId);
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (!student.getSchool().getId().equals(faculty.getSchool().getId())) {
            throw new IllegalArgumentException("Student not in your school");
        }
        validateTeacherAssignedClass(facultyId, student.getClassName(), student.getSection());
        LocalDate date = request.getDate() != null ? request.getDate() : LocalDate.now();

        TeacherDailyActivityReview review = teacherDailyActivityReviewRepository
                .findByFaculty_IdAndStudent_IdAndActivityDate(facultyId, student.getId(), date)
                .orElse(TeacherDailyActivityReview.builder()
                        .faculty(faculty)
                        .student(student)
                        .activityDate(date)
                        .build());
        review.setStatus(request.getStatus());
        review.setNote(request.getNote());
        TeacherDailyActivityReview saved = teacherDailyActivityReviewRepository.save(review);

        return new TeacherDailyActivityDtos.ReviewResponse(
                saved.getId(),
                student.getId(),
                date,
                saved.getStatus().name(),
                saved.getNote()
        );
    }

    private void validateTeacherAssignedClass(Long facultyId, String className, String section) {
        boolean assigned = classSubjectTeacherRepository.findByFaculty_IdAndActiveTrue(facultyId).stream()
                .anyMatch(c -> c.getClassName().equalsIgnoreCase(className) && c.getSection().equalsIgnoreCase(section));
        if (!assigned) {
            throw new IllegalArgumentException("Teacher is not assigned to selected class/section");
        }
    }

    private LocalDate resolveFrom(String range, LocalDate to) {
        String r = range == null ? "TODAY" : range.trim().toUpperCase(Locale.ROOT);
        return switch (r) {
            case "THIS_WEEK", "WEEKLY", "WEEK" -> to.minusDays(6);
            default -> to;
        };
    }

    private double hoursForType(List<DailyLifeActivity> acts, DailyLifeActivityType type) {
        return acts.stream()
                .filter(a -> a.getActivityType() == type)
                .mapToInt(DailyLifeActivity::getDurationMinutes)
                .sum() / 60.0;
    }

    private double averageDaily(List<DailyLifeActivity> acts, DailyLifeActivityType type) {
        if (acts.isEmpty()) return 0;
        Map<LocalDate, Integer> byDay = new HashMap<>();
        for (DailyLifeActivity a : acts) {
            if (a.getActivityType() != type) continue;
            byDay.merge(a.getActivityDate(), a.getDurationMinutes(), Integer::sum);
        }
        return byDay.values().stream().mapToInt(i -> i).average().orElse(0) / 60.0;
    }

    private TeacherDailyActivityStatus computeStatus(List<DailyLifeActivity> activities, TeacherDailyActivityReview review) {
        if (review != null && (review.getStatus() == TeacherDailyActivityStatus.REVIEWED || review.getStatus() == TeacherDailyActivityStatus.FLAGGED)) {
            return review.getStatus();
        }
        if (activities.isEmpty()) return TeacherDailyActivityStatus.NO_LOG_SUBMITTED;
        double study = hoursForType(activities, DailyLifeActivityType.STUDY_HOMEWORK);
        double physical = hoursForType(activities, DailyLifeActivityType.PHYSICAL_ACTIVITY);
        if (study < 1.0) return TeacherDailyActivityStatus.LOW_STUDY_TIME;
        if (physical < 0.5) return TeacherDailyActivityStatus.LOW_PHYSICAL_ACTIVITY;
        if (review != null) return review.getStatus();
        return TeacherDailyActivityStatus.HEALTHY_BALANCE;
    }

    private static String initials(Student s) {
        String a = s.getFirstName() != null && !s.getFirstName().isEmpty() ? s.getFirstName().substring(0, 1) : "";
        String b = s.getLastName() != null && !s.getLastName().isEmpty() ? s.getLastName().substring(0, 1) : "";
        return (a + b).toUpperCase(Locale.ROOT);
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
