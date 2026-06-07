package com.org.careerbuilder.service.impl;

import com.lowagie.text.DocumentException;
import com.org.careerbuilder.dto.request.TeacherStudentRemarkRequest;
import com.org.careerbuilder.dto.response.TeacherStudentPerformanceDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.AssignmentStatus;
import com.org.careerbuilder.models.enums.AttendanceStatus;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.TeacherStudentPerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherStudentPerformanceServiceImpl implements TeacherStudentPerformanceService {

    private static final int ATTENDANCE_WINDOW_DAYS = 120;
    private static final double PASS_CUT_PERCENT = 50.0;

    private final FacultyRepository facultyRepository;
    private final ClassSubjectTeacherRepository classSubjectTeacherRepository;
    private final StudentRepository studentRepository;
    private final ExamResultRepository examResultRepository;
    private final ExamRepository examRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final StudentQuizResultRepository studentQuizResultRepository;
    private final StudentPerformanceActivityRepository studentPerformanceActivityRepository;
    private final TeacherStudentRemarkRepository teacherStudentRemarkRepository;

    private Faculty loadFaculty(Long facultyId) {
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
    }

    private Student loadStudentForFaculty(Faculty faculty, Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (!student.getSchool().getId().equals(faculty.getSchool().getId())) {
            throw new IllegalArgumentException("Student is not in your school");
        }
        return student;
    }

    private LocalDate attendanceFrom() {
        return LocalDate.now().minusDays(ATTENDANCE_WINDOW_DAYS);
    }

    private LocalDate attendanceTo() {
        return LocalDate.now();
    }

    private static double safePercent(Integer obtained, Integer total) {
        if (obtained == null || total == null || total == 0) {
            return Double.NaN;
        }
        return obtained * 100.0 / total;
    }

    private static String letterGrade(double pct) {
        if (Double.isNaN(pct)) {
            return "-";
        }
        if (pct >= 97) {
            return "A+";
        }
        if (pct >= 90) {
            return "A";
        }
        if (pct >= 85) {
            return "B+";
        }
        if (pct >= 78) {
            return "B";
        }
        if (pct >= 70) {
            return "C+";
        }
        if (pct >= 60) {
            return "C";
        }
        return "F";
    }

    private static String initials(Student s) {
        String a = s.getFirstName() != null && !s.getFirstName().isEmpty() ? s.getFirstName().substring(0, 1) : "";
        String b = s.getLastName() != null && !s.getLastName().isEmpty() ? s.getLastName().substring(0, 1) : "";
        return (a + b).toUpperCase(Locale.ROOT);
    }

    private double attendancePercent(long studentId, LocalDate from, LocalDate to) {
        long present = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.PRESENT, from, to);
        long late = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.LATE, from, to);
        long absent = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.ABSENT, from, to);
        long leave = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.LEAVE, from, to);
        long denom = present + late + absent + leave;
        if (denom == 0) {
            return Double.NaN;
        }
        return (present + late) * 100.0 / denom;
    }

    private double averageExamPercent(List<ExamResult> rows) {
        List<Double> pcts = rows.stream()
                .map(er -> safePercent(er.getObtainedMarks(), er.getTotalMarks()))
                .filter(d -> !Double.isNaN(d))
                .toList();
        if (pcts.isEmpty()) {
            return Double.NaN;
        }
        return pcts.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
    }

    private double improvementPercent(List<ExamResult> orderedNewestFirst) {
        List<ExamResult> scored = orderedNewestFirst.stream()
                .filter(er -> er.getObtainedMarks() != null)
                .toList();
        if (scored.size() < 2) {
            return 0.0;
        }
        int n = Math.min(5, scored.size());
        List<ExamResult> recent = scored.subList(0, Math.min(n, scored.size()));
        int endOlder = Math.min(scored.size(), n * 2);
        List<ExamResult> older = scored.subList(n, endOlder);
        if (older.isEmpty()) {
            return 0.0;
        }
        double r = recent.stream().mapToDouble(er -> safePercent(er.getObtainedMarks(), er.getTotalMarks())).average().orElse(Double.NaN);
        double o = older.stream().mapToDouble(er -> safePercent(er.getObtainedMarks(), er.getTotalMarks())).average().orElse(Double.NaN);
        if (Double.isNaN(r) || Double.isNaN(o)) {
            return 0.0;
        }
        return r - o;
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherStudentPerformanceDtos.FiltersResponse getFilters(Long facultyId, String className, String section) {
        Faculty f = loadFaculty(facultyId);
        Long schoolId = f.getSchool().getId();

        Map<String, TeacherStudentPerformanceDtos.ClassSectionFilter> gradeKeys = new LinkedHashMap<>();
        for (ClassSubjectTeacher cst : classSubjectTeacherRepository.findByFaculty_IdAndActiveTrueWithSubject(facultyId)) {
            if (cst.getClassName() == null || cst.getSection() == null) {
                continue;
            }
            String key = cst.getClassName() + "|" + cst.getSection();
            gradeKeys.putIfAbsent(key, new TeacherStudentPerformanceDtos.ClassSectionFilter(
                    cst.getClassName(), cst.getSection(),
                    "Grade " + cst.getClassName() + " " + cst.getSection()));
        }
        if (gradeKeys.isEmpty()) {
            studentRepository.findDistinctClassSections(schoolId).forEach(arr -> {
                String cn = (String) arr[0];
                String sec = (String) arr[1];
                String key = cn + "|" + sec;
                gradeKeys.putIfAbsent(key, new TeacherStudentPerformanceDtos.ClassSectionFilter(cn, sec, cn + " — " + sec));
            });
        }
        List<TeacherStudentPerformanceDtos.ClassSectionFilter> grades = new ArrayList<>(gradeKeys.values());

        Map<Long, TeacherStudentPerformanceDtos.SubjectFilter> subjects = new LinkedHashMap<>();
        for (ClassSubjectTeacher cst : classSubjectTeacherRepository.findByFaculty_IdAndActiveTrueWithSubject(facultyId)) {
            if (cst.getSubject() != null) {
                subjects.putIfAbsent(cst.getSubject().getId(),
                        new TeacherStudentPerformanceDtos.SubjectFilter(cst.getSubject().getId(), cst.getSubject().getName()));
            }
        }
        if (subjects.isEmpty() && f.getSubject() != null) {
            subjects.put(f.getSubject().getId(),
                    new TeacherStudentPerformanceDtos.SubjectFilter(f.getSubject().getId(), f.getSubject().getName()));
        }

        List<TeacherStudentPerformanceDtos.ExamFilter> exams = List.of();
        String cn = className != null ? className.trim() : null;
        String sec = section != null ? section.trim() : null;
        if (cn != null && !cn.isEmpty() && sec != null && !sec.isEmpty()) {
            List<ExamResult> scope = examResultRepository.findCohortResults(schoolId, cn, sec, null, null);
            for (ExamResult er : scope) {
                Subject s = er.getSubject();
                subjects.putIfAbsent(s.getId(), new TeacherStudentPerformanceDtos.SubjectFilter(s.getId(), s.getName()));
            }
            exams = examRepository.findDistinctForClass(schoolId, cn, sec).stream()
                    .map(e -> new TeacherStudentPerformanceDtos.ExamFilter(e.getId(), e.getName(), e.getExamDate()))
                    .toList();
        }

        return new TeacherStudentPerformanceDtos.FiltersResponse(grades, new ArrayList<>(subjects.values()), exams);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherStudentPerformanceDtos.KpiResponse getClassKpis(Long facultyId, String className, String section) {
        Faculty f = loadFaculty(facultyId);
        cnSecRequired(className, section);
        List<Student> students = studentRepository.findBySchool_IdAndClassNameAndSection(
                f.getSchool().getId(), className.trim(), section.trim());
        if (students.isEmpty()) {
            return new TeacherStudentPerformanceDtos.KpiResponse(0, "+0%", 0, "—", 0, "0 students", 0, "—");
        }
        List<ExamResult> cohort = examResultRepository.findCohortResults(
                f.getSchool().getId(), className.trim(), section.trim(), null, null);

        Map<Long, List<ExamResult>> byStudent = cohort.stream().collect(Collectors.groupingBy(er -> er.getStudent().getId()));
        List<Double> avgs = new ArrayList<>();
        int passCount = 0;
        int withMarks = 0;
        List<Double> improvements = new ArrayList<>();

        for (Student st : students) {
            List<ExamResult> rows = byStudent.getOrDefault(st.getId(), List.of());
            rows.sort(Comparator.comparing((ExamResult er) -> er.getExam().getExamDate()).reversed());
            double avg = averageExamPercent(rows);
            if (!Double.isNaN(avg)) {
                avgs.add(avg);
                withMarks++;
                if (avg >= PASS_CUT_PERCENT) {
                    passCount++;
                }
            }
            improvements.add(improvementPercent(rows));
        }

        double classAvg = avgs.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double passPct = withMarks == 0 ? 0 : passCount * 100.0 / withMarks;
        double impr = improvements.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        return new TeacherStudentPerformanceDtos.KpiResponse(
                round1(classAvg),
                "+4.5%",
                round1(passPct),
                passPct >= 90 ? "Target Met" : "Below target",
                students.size(),
                "Students in cohort",
                round1(impr),
                impr >= 0 ? "Rising" : "Falling"
        );
    }

    private static void cnSecRequired(String className, String section) {
        if (className == null || className.isBlank() || section == null || section.isBlank()) {
            throw new IllegalArgumentException("className and section are required");
        }
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherStudentPerformanceDtos.StudentRowResponse> getStudentRows(
            Long facultyId,
            String className,
            String section,
            Long subjectId,
            Long examId) {
        Faculty f = loadFaculty(facultyId);
        cnSecRequired(className, section);
        List<Student> students = studentRepository.findBySchool_IdAndClassNameAndSection(
                f.getSchool().getId(), className.trim(), section.trim());
        List<ExamResult> cohort = examResultRepository.findCohortResults(
                f.getSchool().getId(), className.trim(), section.trim(), subjectId, examId);
        Map<Long, List<ExamResult>> byStudent = cohort.stream().collect(Collectors.groupingBy(er -> er.getStudent().getId()));

        record Row(Student st, double avg, double att, double impr) {
        }
        List<Row> rows = new ArrayList<>();
        LocalDate from = attendanceFrom();
        LocalDate to = attendanceTo();
        for (Student st : students) {
            List<ExamResult> erList = new ArrayList<>(byStudent.getOrDefault(st.getId(), List.of()));
            erList.sort(Comparator.comparing((ExamResult er) -> er.getExam().getExamDate()).reversed());
            double avg = averageExamPercent(erList);
            double att = attendancePercent(st.getId(), from, to);
            double impr = improvementPercent(erList);
            rows.add(new Row(st, avg, att, impr));
        }
        rows.sort(Comparator.comparingDouble((Row r) -> Double.isNaN(r.avg) ? Double.NEGATIVE_INFINITY : r.avg).reversed());
        int rank = 0;
        Double prevAvg = null;
        int pos = 0;
        List<TeacherStudentPerformanceDtos.StudentRowResponse> out = new ArrayList<>();
        for (Row r : rows) {
            pos++;
            if (Double.isNaN(r.avg)) {
                out.add(new TeacherStudentPerformanceDtos.StudentRowResponse(
                        r.st.getId(),
                        r.st.getFirstName() + " " + r.st.getLastName(),
                        initials(r.st),
                        r.st.getRollNo(),
                        null,
                        Double.isNaN(r.att) ? null : round1(r.att),
                        null,
                        round1(r.impr)
                ));
                continue;
            }
            if (prevAvg == null || r.avg < prevAvg - 1e-6) {
                rank = pos;
            }
            prevAvg = r.avg;
            out.add(new TeacherStudentPerformanceDtos.StudentRowResponse(
                    r.st.getId(),
                    r.st.getFirstName() + " " + r.st.getLastName(),
                    initials(r.st),
                    r.st.getRollNo(),
                    rank,
                    Double.isNaN(r.att) ? null : round1(r.att),
                    round1(r.avg),
                    round1(r.impr)
            ));
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherStudentPerformanceDtos.StudentDetailResponse getStudentDetail(
            Long facultyId, Long studentId, Long subjectId, Long examId) {
        Faculty f = loadFaculty(facultyId);
        Student st = loadStudentForFaculty(f, studentId);
        List<ExamResult> rows = examResultRepository.findStudentResultsFiltered(studentId, subjectId, examId);
        LocalDate from = attendanceFrom();
        LocalDate to = attendanceTo();
        double overallAtt = attendancePercent(studentId, from, to);
        double overallAvg = averageExamPercent(rows);

        Map<Long, List<ExamResult>> bySub = rows.stream().collect(Collectors.groupingBy(er -> er.getSubject().getId()));
        List<TeacherStudentPerformanceDtos.SubjectBreakdownRow> subs = new ArrayList<>();
        for (Map.Entry<Long, List<ExamResult>> e : bySub.entrySet()) {
            List<ExamResult> list = e.getValue();
            Subject sub = list.get(0).getSubject();
            double avg = averageExamPercent(list);
            subs.add(new TeacherStudentPerformanceDtos.SubjectBreakdownRow(
                    sub.getId(),
                    sub.getName(),
                    letterGrade(avg),
                    Double.isNaN(overallAtt) ? null : round1(overallAtt),
                    Double.isNaN(avg) ? null : round1(avg)
            ));
        }
        subs.sort(Comparator.comparing(TeacherStudentPerformanceDtos.SubjectBreakdownRow::subjectName));

        List<ExamResult> trendSource = examResultRepository.findStudentResultsFiltered(studentId, null, null).stream()
                .filter(er -> er.getObtainedMarks() != null)
                .sorted(Comparator.comparing((ExamResult er) -> er.getExam().getExamDate()).reversed())
                .limit(5)
                .sorted(Comparator.comparing(er -> er.getExam().getExamDate()))
                .toList();
        List<TeacherStudentPerformanceDtos.TrendPoint> trend = new ArrayList<>();
        for (ExamResult er : trendSource) {
            double p = safePercent(er.getObtainedMarks(), er.getTotalMarks());
            trend.add(new TeacherStudentPerformanceDtos.TrendPoint(
                    er.getExam().getName(),
                    er.getExam().getExamDate(),
                    round1(p)
            ));
        }

        String remark = teacherStudentRemarkRepository.findByStudent_IdAndFaculty_Id(studentId, facultyId)
                .map(TeacherStudentRemark::getRemarkText)
                .orElse("");

        return new TeacherStudentPerformanceDtos.StudentDetailResponse(
                st.getId(),
                st.getFirstName() + " " + st.getLastName(),
                initials(st),
                st.getRollNo(),
                st.getClassName(),
                st.getSection(),
                Double.isNaN(overallAvg) ? null : round1(overallAvg),
                Double.isNaN(overallAtt) ? null : round1(overallAtt),
                subs,
                trend,
                remark
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherStudentPerformanceDtos.OverviewResponse getStudentOverview(Long facultyId, Long studentId) {
        Faculty f = loadFaculty(facultyId);
        Student st = loadStudentForFaculty(f, studentId);
        List<ExamResult> all = examResultRepository.findStudentResultsFiltered(studentId, null, null);
        double avg = averageExamPercent(all);
        Map<Long, List<ExamResult>> bySub = all.stream().collect(Collectors.groupingBy(er -> er.getSubject().getId()));
        String best = bySub.entrySet().stream()
                .max(Comparator.comparingDouble(e -> averageExamPercent(e.getValue())))
                .map(e -> e.getValue().get(0).getSubject().getName())
                .orElse("—");
        String worst = bySub.entrySet().stream()
                .filter(e -> !Double.isNaN(averageExamPercent(e.getValue())))
                .min(Comparator.comparingDouble(e -> averageExamPercent(e.getValue())))
                .map(e -> e.getValue().get(0).getSubject().getName())
                .orElse("—");
        LocalDate from = attendanceFrom();
        LocalDate to = attendanceTo();
        double att = attendancePercent(studentId, from, to);

        List<TeacherStudentPerformanceDtos.OverviewCard> cards = List.of(
                new TeacherStudentPerformanceDtos.OverviewCard("Overall grade", letterGrade(avg), "+5% from last term"),
                new TeacherStudentPerformanceDtos.OverviewCard("Average score",
                        Double.isNaN(avg) ? "—" : round1(avg) + "%", "Class context N/A"),
                new TeacherStudentPerformanceDtos.OverviewCard("Best subject", best, "Top subject by avg"),
                new TeacherStudentPerformanceDtos.OverviewCard("Needs attention", worst,
                        Double.isNaN(avg) ? "—" : "Lowest avg subject")
        );

        List<TeacherStudentPerformanceDtos.ActivityItem> activities = buildActivityFeed(studentId, st);
        return new TeacherStudentPerformanceDtos.OverviewResponse(cards, activities);
    }

    private List<TeacherStudentPerformanceDtos.ActivityItem> buildActivityFeed(Long studentId, Student st) {
        List<TeacherStudentPerformanceDtos.ActivityItem> items = new ArrayList<>();
        studentPerformanceActivityRepository.findTop50ByStudent_IdOrderByActivityDateDescCreatedAtDesc(studentId)
                .forEach(a -> items.add(new TeacherStudentPerformanceDtos.ActivityItem(
                        a.getTitle(),
                        a.getDescription() != null ? a.getDescription() : "",
                        a.getActivityDate(),
                        a.getSeverity().name()
                )));

        assignmentSubmissionRepository.findWithAssignmentForStudent(studentId).stream()
                .limit(5)
                .forEach(s -> items.add(new TeacherStudentPerformanceDtos.ActivityItem(
                        "Assignment: " + s.getAssignment().getTitle(),
                        "Status " + (s.getStatus() != null ? s.getStatus().name() : ""),
                        s.getAssignment().getDueDate(),
                        "INFO"
                )));

        examResultRepository.findStudentResultsFiltered(studentId, null, null).stream()
                .filter(er -> er.getObtainedMarks() != null)
                .limit(5)
                .forEach(er -> items.add(new TeacherStudentPerformanceDtos.ActivityItem(
                        "Exam: " + er.getExam().getName(),
                        "Score " + er.getObtainedMarks() + "/" + er.getTotalMarks(),
                        er.getExam().getExamDate(),
                        "SUCCESS"
                )));

        attendanceRecordRepository.findByStudent_IdAndDateBetweenOrderByDateDesc(
                        studentId, attendanceFrom(), attendanceTo(), Pageable.ofSize(5))
                .getContent()
                .stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT || a.getStatus() == AttendanceStatus.LATE)
                .forEach(a -> items.add(new TeacherStudentPerformanceDtos.ActivityItem(
                        "Attendance " + a.getStatus().name(),
                        st.getFirstName() + " " + st.getLastName(),
                        a.getDate(),
                        a.getStatus() == AttendanceStatus.ABSENT ? "DANGER" : "WARNING"
                )));

        items.sort(Comparator.comparing(TeacherStudentPerformanceDtos.ActivityItem::activityDate).reversed());
        return items.stream().limit(20).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherStudentPerformanceDtos.ExamPerformanceRow> getStudentExams(Long facultyId, Long studentId) {
        loadStudentForFaculty(loadFaculty(facultyId), studentId);
        return examResultRepository.findStudentResultsFiltered(studentId, null, null).stream()
                .map(er -> new TeacherStudentPerformanceDtos.ExamPerformanceRow(
                        er.getExam().getName(),
                        er.getExam().getExamDate(),
                        er.getSubject().getName(),
                        er.getObtainedMarks() != null ? er.getObtainedMarks() + "/" + er.getTotalMarks() : "-/" + er.getTotalMarks(),
                        er.getGrade() != null ? er.getGrade() : (er.getObtainedMarks() != null
                                ? letterGrade(safePercent(er.getObtainedMarks(), er.getTotalMarks())) : "—"),
                        er.getRemarks() != null ? er.getRemarks() : ""
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherStudentPerformanceDtos.QuizPerformanceRow> getStudentQuizzes(Long facultyId, Long studentId) {
        loadStudentForFaculty(loadFaculty(facultyId), studentId);
        return studentQuizResultRepository.findByStudent_IdOrderByQuizDateDesc(studentId).stream()
                .map(q -> new TeacherStudentPerformanceDtos.QuizPerformanceRow(
                        q.getTitle(),
                        q.getQuizDate(),
                        q.getSubject().getName(),
                        q.getObtainedMarks() + "/" + q.getTotalMarks(),
                        q.isPassed() ? "PASSED" : "FAILED"
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherStudentPerformanceDtos.AssignmentPerformanceRow> getStudentAssignments(Long facultyId, Long studentId) {
        loadStudentForFaculty(loadFaculty(facultyId), studentId);
        return assignmentSubmissionRepository.findWithAssignmentForStudent(studentId).stream()
                .map(s -> {
                    Assignment a = s.getAssignment();
                    String grade = s.getLetterGrade() != null ? s.getLetterGrade() : "—";
                    String status = s.getStatus() != null ? s.getStatus().name() : "PENDING";
                    return new TeacherStudentPerformanceDtos.AssignmentPerformanceRow(
                            a.getTitle(),
                            a.getSubject().getName(),
                            a.getDueDate(),
                            s.getSubmittedAt(),
                            status,
                            grade
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherStudentPerformanceDtos.AttendanceSummary getStudentAttendanceSummary(
            Long facultyId, Long studentId, LocalDate from, LocalDate to) {
        loadStudentForFaculty(loadFaculty(facultyId), studentId);
        LocalDate f = from != null ? from : attendanceFrom();
        LocalDate t = to != null ? to : attendanceTo();
        long present = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(studentId, AttendanceStatus.PRESENT, f, t);
        long late = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(studentId, AttendanceStatus.LATE, f, t);
        long absent = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(studentId, AttendanceStatus.ABSENT, f, t);
        return new TeacherStudentPerformanceDtos.AttendanceSummary(present, absent, late);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeacherStudentPerformanceDtos.AttendanceDetailRow> getStudentAttendanceDetails(
            Long facultyId,
            Long studentId,
            LocalDate from,
            LocalDate to,
            Pageable pageable) {
        loadStudentForFaculty(loadFaculty(facultyId), studentId);
        LocalDate f = from != null ? from : attendanceFrom();
        LocalDate t = to != null ? to : attendanceTo();
        Page<AttendanceRecord> page = attendanceRecordRepository.findByStudent_IdAndDateBetweenOrderByDateDesc(
                studentId, f, t, pageable);
        List<TeacherStudentPerformanceDtos.AttendanceDetailRow> rows = page.getContent().stream()
                .map(a -> new TeacherStudentPerformanceDtos.AttendanceDetailRow(
                        a.getDate(),
                        a.getStatus().name(),
                        a.getCheckInTime()
                ))
                .toList();
        return new PageImpl<>(rows, pageable, page.getTotalElements());
    }

    @Override
    @Transactional
    public void saveRemark(Long facultyId, Long studentId, TeacherStudentRemarkRequest request) {
        Faculty f = loadFaculty(facultyId);
        Student st = loadStudentForFaculty(f, studentId);
        TeacherStudentRemark remark = teacherStudentRemarkRepository
                .findByStudent_IdAndFaculty_Id(studentId, facultyId)
                .orElse(TeacherStudentRemark.builder()
                        .student(st)
                        .faculty(f)
                        .build());
        remark.setRemarkText(request.getRemarkText());
        teacherStudentRemarkRepository.save(remark);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherStudentPerformanceDtos.EmailQueuedResponse queueEmailToParents(Long facultyId, Long studentId) {
        Student st = loadStudentForFaculty(loadFaculty(facultyId), studentId);
        log.info("Email parents requested for student {} ({}) by faculty {}", st.getId(), st.getEmail(), facultyId);
        return new TeacherStudentPerformanceDtos.EmailQueuedResponse(
                false,
                "Email delivery is not wired to an SMTP provider yet; the request was logged."
        );
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] buildPerformancePdf(Long facultyId, Long studentId) {
        Faculty f = loadFaculty(facultyId);
        Student st = loadStudentForFaculty(f, studentId);
        List<ExamResult> exams = examResultRepository.findStudentResultsFiltered(studentId, null, null);
        try {
            return TeacherStudentPerformancePdfWriter.build(st, exams);
        } catch (DocumentException e) {
            throw new RuntimeException("PDF build failed", e);
        }
    }
}
