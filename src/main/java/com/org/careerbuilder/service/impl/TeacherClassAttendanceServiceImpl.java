package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.ClassAttendanceLineWrite;
import com.org.careerbuilder.dto.request.ClassAttendanceSaveDraftRequest;
import com.org.careerbuilder.dto.request.ClassAttendanceSubmitRequest;
import com.org.careerbuilder.dto.response.TeacherClassAttendanceDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.AttendanceStatus;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.TeacherClassAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherClassAttendanceServiceImpl implements TeacherClassAttendanceService {

    private static final DateTimeFormatter ISO_DT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final FacultyRepository facultyRepository;
    private final ClassSubjectTeacherRepository classSubjectTeacherRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final ClassAttendanceSessionRepository sessionRepository;
    private final ClassAttendanceLineRepository lineRepository;
    private final ClassAttendanceEditAuditRepository auditRepository;

    @Override
    @Transactional(readOnly = true)
    public TeacherClassAttendanceDtos.FiltersResponse getFilters(Long facultyId) {
        loadFaculty(facultyId);
        List<ClassSubjectTeacher> csts = classSubjectTeacherRepository.findByFaculty_IdAndActiveTrue(facultyId);
        Map<String, TeacherClassAttendanceDtos.ClassSectionOption> classKeys = new LinkedHashMap<>();
        Map<Long, TeacherClassAttendanceDtos.SubjectOption> subjects = new LinkedHashMap<>();
        for (ClassSubjectTeacher cst : csts) {
            String key = cst.getClassName().toLowerCase(Locale.ROOT) + "|" + cst.getSection().toLowerCase(Locale.ROOT);
            classKeys.putIfAbsent(key, new TeacherClassAttendanceDtos.ClassSectionOption(
                    cst.getClassName(),
                    cst.getSection(),
                    "Grade " + cst.getClassName() + " " + cst.getSection()
            ));
            if (cst.getSubject() != null) {
                subjects.putIfAbsent(cst.getSubject().getId(),
                        new TeacherClassAttendanceDtos.SubjectOption(cst.getSubject().getId(), cst.getSubject().getName()));
            }
        }
        return new TeacherClassAttendanceDtos.FiltersResponse(
                new ArrayList<>(classKeys.values()),
                new ArrayList<>(subjects.values())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherClassAttendanceDtos.RosterResponse getRoster(
            Long facultyId, String className, String section, Long subjectId, LocalDate sessionDate) {
        Faculty faculty = loadFaculty(facultyId);
        assertTeaches(facultyId, faculty.getSchool().getId(), className, section, subjectId);
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        Optional<ClassAttendanceSession> sessionOpt = sessionRepository
                .findByFaculty_IdAndClassNameIgnoreCaseAndSectionIgnoreCaseAndSubject_IdAndSessionDate(
                        facultyId, className, section, subjectId, sessionDate);
        List<Student> students = loadStudentsOrdered(faculty.getSchool().getId(), className, section);
        Map<Long, ClassAttendanceLine> byStudent = new HashMap<>();
        sessionOpt.ifPresent(s -> {
            for (ClassAttendanceLine line : lineRepository.findBySession_IdOrderByStudent_RollNoAsc(s.getId())) {
                byStudent.put(line.getStudent().getId(), line);
            }
        });
        return buildRosterResponse(
                sessionOpt.orElse(null),
                facultyId,
                className,
                section,
                subject,
                sessionDate,
                students,
                byStudent
        );
    }

    @Override
    @Transactional
    public TeacherClassAttendanceDtos.RosterResponse saveDraft(Long facultyId, ClassAttendanceSaveDraftRequest request) {
        Faculty faculty = loadFaculty(facultyId);
        assertTeaches(facultyId, faculty.getSchool().getId(), request.getClassName(), request.getSection(), request.getSubjectId());
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        ClassAttendanceSession session = sessionRepository
                .findByFaculty_IdAndClassNameIgnoreCaseAndSectionIgnoreCaseAndSubject_IdAndSessionDate(
                        facultyId, request.getClassName(), request.getSection(), request.getSubjectId(), request.getSessionDate())
                .orElseGet(() -> newSession(faculty, request.getClassName(), request.getSection(), subject, request.getSessionDate()));

        if (session.isLocked()) {
            if (request.getEditReason() == null || request.getEditReason().isBlank()) {
                throw new IllegalArgumentException("editReason is required when updating a locked attendance session");
            }
            auditRepository.save(ClassAttendanceEditAudit.builder()
                    .session(session)
                    .faculty(faculty)
                    .reasonText(request.getEditReason().trim())
                    .build());
        }

        replaceLines(session, request.getRows(), faculty.getSchool().getId(), request.getClassName(), request.getSection());
        sessionRepository.save(session);

        return getRoster(facultyId, request.getClassName(), request.getSection(), request.getSubjectId(), request.getSessionDate());
    }

    @Override
    @Transactional
    public TeacherClassAttendanceDtos.SubmitResponse submit(
            Long facultyId, ClassAttendanceSubmitRequest request, MultipartFile proof) {
        Faculty faculty = loadFaculty(facultyId);
        assertTeaches(facultyId, faculty.getSchool().getId(), request.getClassName(), request.getSection(), request.getSubjectId());
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        ClassAttendanceSession session = sessionRepository
                .findByFaculty_IdAndClassNameIgnoreCaseAndSectionIgnoreCaseAndSubject_IdAndSessionDate(
                        facultyId, request.getClassName(), request.getSection(), request.getSubjectId(), request.getSessionDate())
                .orElseGet(() -> newSession(faculty, request.getClassName(), request.getSection(), subject, request.getSessionDate()));

        if (session.isLocked()) {
            throw new IllegalStateException("Attendance for this class, subject and date is already submitted (locked).");
        }

        replaceLines(session, request.getRows(), faculty.getSchool().getId(), request.getClassName(), request.getSection());
        if (proof != null && !proof.isEmpty()) {
            session.setProofAttachmentPath(storeProof(facultyId, proof));
        }
        session.setLocked(true);
        session.setLockedAt(LocalDateTime.now());
        sessionRepository.save(session);

        List<Student> students = loadStudentsOrdered(faculty.getSchool().getId(), request.getClassName(), request.getSection());
        Map<Long, ClassAttendanceLine> byStudent = new HashMap<>();
        for (ClassAttendanceLine line : lineRepository.findBySession_IdOrderByStudent_RollNoAsc(session.getId())) {
            byStudent.put(line.getStudent().getId(), line);
        }
        Counts c = countStatuses(students, byStudent);
        return new TeacherClassAttendanceDtos.SubmitResponse(
                session.getId(),
                true,
                c.present,
                c.absent,
                c.late,
                c.leave,
                c.notMarked,
                students.size()
        );
    }

    @Override
    @Transactional
    public TeacherClassAttendanceDtos.RosterResponse markAllPresent(
            Long facultyId, String className, String section, Long subjectId, LocalDate sessionDate) {
        Faculty faculty = loadFaculty(facultyId);
        assertTeaches(facultyId, faculty.getSchool().getId(), className, section, subjectId);
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        ClassAttendanceSession session = sessionRepository
                .findByFaculty_IdAndClassNameIgnoreCaseAndSectionIgnoreCaseAndSubject_IdAndSessionDate(
                        facultyId, className, section, subjectId, sessionDate)
                .orElseGet(() -> newSession(faculty, className, section, subject, sessionDate));
        if (session.isLocked()) {
            throw new IllegalStateException("Cannot mark all present on a locked session");
        }
        List<ClassAttendanceLineWrite> rows = loadStudentsOrdered(faculty.getSchool().getId(), className, section).stream()
                .map(st -> ClassAttendanceLineWrite.builder()
                        .studentId(st.getId())
                        .status(AttendanceStatus.PRESENT)
                        .build())
                .toList();
        replaceLines(session, rows, faculty.getSchool().getId(), className, section);
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);
        return getRoster(facultyId, className, section, subjectId, sessionDate);
    }

    @Override
    @Transactional
    public void resetMarks(Long facultyId, String className, String section, Long subjectId, LocalDate sessionDate) {
        loadFaculty(facultyId);
        ClassAttendanceSession session = sessionRepository
                .findByFaculty_IdAndClassNameIgnoreCaseAndSectionIgnoreCaseAndSubject_IdAndSessionDate(
                        facultyId, className, section, subjectId, sessionDate)
                .orElse(null);
        if (session == null) {
            return;
        }
        if (session.isLocked()) {
            throw new IllegalStateException("Cannot reset a locked attendance session");
        }
        lineRepository.deleteBySession_Id(session.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherClassAttendanceDtos.ImportPreviewResponse importPreview(
            Long facultyId, String className, String section, Long subjectId, LocalDate sessionDate, MultipartFile file) {
        Faculty faculty = loadFaculty(facultyId);
        assertTeaches(facultyId, faculty.getSchool().getId(), className, section, subjectId);
        List<Student> students = loadStudentsOrdered(faculty.getSchool().getId(), className, section);
        Map<Integer, Student> byRoll = students.stream().collect(Collectors.toMap(Student::getRollNo, s -> s, (a, b) -> a));

        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        List<TeacherClassAttendanceDtos.ImportPreviewRow> rows = new ArrayList<>();

        if (filename.endsWith(".csv") || "text/csv".equalsIgnoreCase(file.getContentType())) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("[,;\\t]");
                    if (parts.length < 2) {
                        continue;
                    }
                    try {
                        int roll = Integer.parseInt(parts[0].trim().replaceAll("[^0-9]", ""));
                        String st = parts[1].trim().toUpperCase(Locale.ROOT);
                        Student stu = byRoll.get(roll);
                        if (stu == null) {
                            continue;
                        }
                        String suggested = normalizeCsvStatus(st);
                        rows.add(new TeacherClassAttendanceDtos.ImportPreviewRow(
                                roll,
                                stu.getId(),
                                (stu.getFirstName() + " " + stu.getLastName()).trim(),
                                suggested,
                                0.95
                        ));
                    } catch (NumberFormatException ignored) {
                        // header
                    }
                }
            } catch (Exception e) {
                return new TeacherClassAttendanceDtos.ImportPreviewResponse(
                        List.of(), "Could not read CSV: " + e.getMessage());
            }
            return new TeacherClassAttendanceDtos.ImportPreviewResponse(
                    rows, rows.isEmpty() ? "No rows parsed. Use columns: roll,status (PRESENT/ABSENT/LATE/LEAVE)." : "Parsed from CSV.");
        }
        return new TeacherClassAttendanceDtos.ImportPreviewResponse(
                List.of(),
                "OCR / image import is not configured. Upload a CSV with columns: roll number, status.");
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherClassAttendanceDtos.HistoryPage getHistory(
            Long facultyId,
            LocalDate from,
            LocalDate to,
            String className,
            String section,
            Long subjectId,
            Pageable pageable) {
        loadFaculty(facultyId);
        LocalDate f = from != null ? from : LocalDate.now().minusMonths(3);
        LocalDate t = to != null ? to : LocalDate.now();
        Page<ClassAttendanceSession> page = sessionRepository.searchHistory(
                facultyId, f, t,
                blankToNull(className),
                blankToNull(section),
                subjectId,
                pageable
        );
        List<TeacherClassAttendanceDtos.HistoryRow> rows = new ArrayList<>();
        for (ClassAttendanceSession s : page.getContent()) {
            int total = (int) studentRepository.countBySchool_IdAndClassNameAndSection(
                    s.getSchoolId(), s.getClassName(), s.getSection());
            Map<Long, ClassAttendanceLine> byStudent = lineRepository.findBySession_IdOrderByStudent_RollNoAsc(s.getId()).stream()
                    .collect(Collectors.toMap(l -> l.getStudent().getId(), l -> l));
            int present = (int) byStudent.values().stream().filter(l -> l.getStatus() == AttendanceStatus.PRESENT).count();
            String taken = shortFacultyName(s.getFaculty());
            rows.add(new TeacherClassAttendanceDtos.HistoryRow(
                    s.getId(),
                    s.getSessionDate(),
                    gradeDisplay(s.getClassName(), s.getSection()),
                    s.getClassName(),
                    s.getSection(),
                    s.getSubject().getName(),
                    present,
                    Math.max(total, byStudent.size()),
                    taken,
                    s.getProofAttachmentPath() != null && !s.getProofAttachmentPath().isBlank(),
                    s.isLocked()
            ));
        }
        return new TeacherClassAttendanceDtos.HistoryPage(rows, page.getTotalPages(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Resource exportHistoryCsv(
            Long facultyId,
            LocalDate from,
            LocalDate to,
            String className,
            String section,
            Long subjectId) {
        TeacherClassAttendanceDtos.HistoryPage page = getHistory(
                facultyId, from, to, className, section, subjectId, PageRequest.of(0, 5000));
        StringBuilder sb = new StringBuilder("sessionId,date,class,section,subject,present,total,takenBy,hasProof,locked\n");
        DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;
        for (TeacherClassAttendanceDtos.HistoryRow r : page.content()) {
            sb.append(r.sessionId()).append(',')
                    .append(r.date().format(df)).append(',')
                    .append(csvEscape(r.className())).append(',')
                    .append(csvEscape(r.section())).append(',')
                    .append(csvEscape(r.subjectName())).append(',')
                    .append(r.presentCount()).append(',')
                    .append(r.totalStudents()).append(',')
                    .append(csvEscape(r.takenByShort())).append(',')
                    .append(r.hasProof()).append(',')
                    .append(r.locked()).append('\n');
        }
        return new ByteArrayResource(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherClassAttendanceDtos.SessionDetailResponse getSessionDetail(Long facultyId, Long sessionId) {
        ClassAttendanceSession s = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        if (!s.getFaculty().getId().equals(facultyId)) {
            throw new IllegalArgumentException("Session belongs to another teacher");
        }
        List<Student> students = loadStudentsOrdered(s.getSchoolId(), s.getClassName(), s.getSection());
        Map<Long, ClassAttendanceLine> byStudent = lineRepository.findBySession_IdOrderByStudent_RollNoAsc(s.getId()).stream()
                .collect(Collectors.toMap(l -> l.getStudent().getId(), l -> l));
        List<TeacherClassAttendanceDtos.RosterStudentRow> rosterRows = students.stream()
                .map(st -> toRosterRow(st, byStudent.get(st.getId())))
                .toList();
        List<TeacherClassAttendanceDtos.EditAuditItem> audits = auditRepository.findTop20BySession_IdOrderByEditedAtDesc(sessionId).stream()
                .map(a -> new TeacherClassAttendanceDtos.EditAuditItem(
                        a.getReasonText(),
                        ISO_DT.format(a.getEditedAt()),
                        shortFacultyName(a.getFaculty())
                ))
                .toList();
        return new TeacherClassAttendanceDtos.SessionDetailResponse(
                s.getId(),
                s.getSessionDate(),
                s.getClassName(),
                s.getSection(),
                s.getSubject().getId(),
                s.getSubject().getName(),
                s.isLocked(),
                s.getProofAttachmentPath(),
                rosterRows,
                audits
        );
    }

    // --- helpers ---

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }

    private static String csvEscape(String s) {
        if (s == null) {
            return "";
        }
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static String normalizeCsvStatus(String st) {
        if (st.contains("LATE")) {
            return "LATE";
        }
        if (st.contains("ABS")) {
            return "ABSENT";
        }
        if (st.contains("LEAVE")) {
            return "LEAVE";
        }
        return "PRESENT";
    }

    private String storeProof(Long facultyId, MultipartFile file) {
        try {
            Path dir = Paths.get("uploads", "class-attendance-proofs", String.valueOf(facultyId));
            Files.createDirectories(dir);
            String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "proof";
            String safe = original.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = dir.resolve(UUID.randomUUID() + "_" + safe);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString().replace("\\", "/");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store proof file", e);
        }
    }

    private ClassAttendanceSession newSession(Faculty faculty, String className, String section, Subject subject, LocalDate date) {
        ClassAttendanceSession s = ClassAttendanceSession.builder()
                .faculty(faculty)
                .schoolId(faculty.getSchool().getId())
                .className(className.trim())
                .section(section.trim())
                .subject(subject)
                .sessionDate(date)
                .locked(false)
                .build();
        return sessionRepository.save(s);
    }

    private void replaceLines(
            ClassAttendanceSession session,
            List<ClassAttendanceLineWrite> rows,
            Long schoolId,
            String className,
            String section) {
        lineRepository.deleteBySession_Id(session.getId());
        List<ClassAttendanceLine> toSave = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (ClassAttendanceLineWrite r : rows) {
            if (!seen.add(r.getStudentId())) {
                continue;
            }
            Student st = studentRepository.findById(r.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + r.getStudentId()));
            if (!st.getSchool().getId().equals(schoolId)
                    || !st.getClassName().equalsIgnoreCase(className.trim())
                    || !st.getSection().equalsIgnoreCase(section.trim())) {
                throw new IllegalArgumentException("Student " + r.getStudentId() + " is not in this class");
            }
            toSave.add(ClassAttendanceLine.builder()
                    .session(session)
                    .student(st)
                    .status(r.getStatus())
                    .remarks(r.getRemarks())
                    .build());
        }
        lineRepository.saveAll(toSave);
    }

    private TeacherClassAttendanceDtos.RosterResponse buildRosterResponse(
            ClassAttendanceSession session,
            Long facultyId,
            String className,
            String section,
            Subject subject,
            LocalDate sessionDate,
            List<Student> students,
            Map<Long, ClassAttendanceLine> byStudent) {

        Counts c = countStatuses(students, byStudent);
        List<TeacherClassAttendanceDtos.RosterStudentRow> rosterRows = students.stream()
                .map(st -> toRosterRow(st, byStudent.get(st.getId())))
                .toList();
        return new TeacherClassAttendanceDtos.RosterResponse(
                session != null ? session.getId() : null,
                sessionDate,
                className,
                section,
                subject.getId(),
                subject.getName(),
                session != null && session.isLocked(),
                c.present,
                c.absent,
                c.late,
                c.leave,
                c.notMarked,
                rosterRows
        );
    }

    private TeacherClassAttendanceDtos.RosterStudentRow toRosterRow(Student st, ClassAttendanceLine line) {
        String status = line == null ? "NOT_MARKED" : line.getStatus().name();
        String fn = st.getFirstName() == null ? "" : st.getFirstName();
        String ln = st.getLastName() == null ? "" : st.getLastName();
        String i1 = fn.isEmpty() ? "?" : fn.substring(0, 1);
        String i2 = ln.isEmpty() ? "" : ln.substring(0, 1);
        String initials = (i1 + i2).toUpperCase(Locale.ROOT);
        return new TeacherClassAttendanceDtos.RosterStudentRow(
                st.getId(),
                st.getRollNo(),
                (st.getFirstName() + " " + st.getLastName()).trim(),
                initials,
                status
        );
    }

    private record Counts(int present, int absent, int late, int leave, int notMarked) {}

    private Counts countStatuses(List<Student> students, Map<Long, ClassAttendanceLine> byStudent) {
        int p = 0, a = 0, l = 0, lv = 0, nm = 0;
        for (Student st : students) {
            ClassAttendanceLine line = byStudent.get(st.getId());
            if (line == null) {
                nm++;
            } else {
                switch (line.getStatus()) {
                    case PRESENT -> p++;
                    case ABSENT -> a++;
                    case LATE -> l++;
                    case LEAVE -> lv++;
                }
            }
        }
        return new Counts(p, a, l, lv, nm);
    }

    private List<Student> loadStudentsOrdered(Long schoolId, String className, String section) {
        List<Student> students = studentRepository.findBySchool_IdAndClassNameAndSection(schoolId, className, section);
        students.sort(Comparator.comparing(Student::getRollNo));
        return students;
    }

    private Faculty loadFaculty(Long facultyId) {
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
    }

    private void assertTeaches(Long facultyId, Long schoolId, String className, String section, Long subjectId) {
        List<ClassSubjectTeacher> list = classSubjectTeacherRepository.findBySchoolIdAndClassNameAndSectionAndSubject_IdAndActiveTrue(
                schoolId, className.trim(), section.trim(), subjectId);
        boolean ok = list.stream().anyMatch(c -> c.getFaculty().getId().equals(facultyId));
        if (!ok) {
            throw new IllegalArgumentException("Teacher is not assigned to this class and subject");
        }
    }

    private static String gradeDisplay(String className, String section) {
        if (className == null) {
            return "";
        }
        String c = className.trim();
        String s = section == null ? "" : section.trim();
        if (c.toLowerCase(Locale.ROOT).startsWith("grade")) {
            return c + " " + s;
        }
        return "Grade " + c + " " + s;
    }

    private static String shortFacultyName(Faculty f) {
        String fn = f.getFirstName() == null ? "" : f.getFirstName().trim();
        if (fn.isEmpty()) {
            return "Teacher";
        }
        return "Prof. " + fn;
    }
}
