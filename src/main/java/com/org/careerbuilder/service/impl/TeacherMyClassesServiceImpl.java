package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.*;
import com.org.careerbuilder.dto.response.TeacherMyClassesDtos;
import com.org.careerbuilder.dto.response.TeacherScheduleDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.AttendanceStatus;
import com.org.careerbuilder.models.enums.TeacherActivityType;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.TeacherMyClassesService;
import com.org.careerbuilder.service.TeacherScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TeacherMyClassesServiceImpl implements TeacherMyClassesService {

    public static final String KIND_TIMETABLE_SLOT = "TIMETABLE_SLOT";
    public static final String KIND_TEACHER_ENTRY = "TEACHER_ENTRY";

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TIME_FMT_12 = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    private final FacultyRepository facultyRepository;
    private final TeacherScheduleService teacherScheduleService;
    private final ClassScheduleSlotRepository classScheduleSlotRepository;
    private final TeacherScheduleEntryRepository teacherScheduleEntryRepository;
    private final ClassSubjectTeacherRepository classSubjectTeacherRepository;
    private final TeacherSessionNoteRepository teacherSessionNoteRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final SubjectRepository subjectRepository;
    private final AssignmentRepository assignmentRepository;
    private final TeacherNoticeRepository teacherNoticeRepository;

    @Override
    @Transactional(readOnly = true)
    public TeacherMyClassesDtos.ClassSessionListResponse listSessions(
            Long facultyId, String timeRange, String academicYear, String search) {
        Faculty faculty = loadFaculty(facultyId);
        LocalDate today = LocalDate.now();
        List<LocalDate> dates = datesForRange(normalizeRange(timeRange), today);
        LocalDate[] ay = parseAcademicYear(academicYear);
        if (ay != null) {
            dates = dates.stream().filter(d -> !d.isBefore(ay[0]) && !d.isAfter(ay[1])).toList();
        }

        List<TeacherMyClassesDtos.ClassSessionCard> cards = new ArrayList<>();
        for (LocalDate d : dates) {
            TeacherScheduleDtos.DayScheduleResponse day = teacherScheduleService.getDaySchedule(facultyId, d, null, null);
            for (TeacherScheduleDtos.ScheduleItemResponse item : day.items()) {
                Optional<TeacherMyClassesDtos.ClassSessionCard> card = mapItemToCard(faculty, item, d);
                card.ifPresent(cards::add);
            }
        }
        cards.sort(Comparator.comparing(TeacherMyClassesDtos.ClassSessionCard::sessionDate)
                .thenComparing(TeacherMyClassesDtos.ClassSessionCard::startTime));

        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase(Locale.ROOT);
            cards = cards.stream()
                    .filter(c -> Stream.of(
                                    c.gradeDisplay(),
                                    c.subjectName(),
                                    c.lastTopic(),
                                    c.className(),
                                    c.section())
                            .filter(Objects::nonNull)
                            .map(s -> s.toLowerCase(Locale.ROOT))
                            .anyMatch(s -> s.contains(q)))
                    .toList();
        }
        return new TeacherMyClassesDtos.ClassSessionListResponse(cards);
    }

    @Override
    @Transactional
    public TeacherMyClassesDtos.ExtraSessionCreatedResponse createExtraSession(Long facultyId, TeacherExtraSessionRequest request) {
        Faculty faculty = loadFaculty(facultyId);
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        String title = "Extra: " + subject.getName();
        String notes = request.getReason() == null || request.getReason().isBlank()
                ? "Extra session"
                : "Extra session — " + request.getReason().trim();

        TeacherScheduleEntryRequest scheduleReq = TeacherScheduleEntryRequest.builder()
                .title(title)
                .activityType(TeacherActivityType.CLASS)
                .recurring(false)
                .specificDate(request.getSessionDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .className(request.getClassName().trim())
                .section(request.getSection().trim())
                .notes(notes)
                .subjectId(subject.getId())
                .substituteForName(request.getSubstituteForName())
                .build();
        var created = teacherScheduleService.createEntry(facultyId, scheduleReq);
        return new TeacherMyClassesDtos.ExtraSessionCreatedResponse(created.id());
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherMyClassesDtos.SessionHeaderResponse getSessionHeader(
            Long facultyId, String sessionKind, Long refId, LocalDate sessionDate) {
        ResolvedSession rs = resolveSessionOrThrow(facultyId, sessionKind, refId, sessionDate);
        Faculty faculty = loadFaculty(facultyId);
        return new TeacherMyClassesDtos.SessionHeaderResponse(
                rs.kind(),
                rs.refId(),
                rs.date(),
                rs.start(),
                rs.end(),
                timeRange(rs.start(), rs.end()),
                gradeDisplay(rs.className(), rs.section()),
                rs.className(),
                rs.section(),
                rs.subject() != null ? rs.subject().getId() : null,
                rs.subject() != null ? rs.subject().getName() : "",
                rs.substituteForDisplay(),
                rs.extraSession()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherMyClassesDtos.LessonNoteResponse getLessonNotes(
            Long facultyId, String sessionKind, Long refId, LocalDate sessionDate) {
        resolveSessionOrThrow(facultyId, sessionKind, refId, sessionDate);
        return teacherSessionNoteRepository
                .findByFaculty_IdAndSessionKindAndRefIdAndSessionDate(facultyId, sessionKind, refId, sessionDate)
                .map(n -> new TeacherMyClassesDtos.LessonNoteResponse(
                        n.getTopicCovered(), n.getDescriptionNotes(), n.getHomework()))
                .orElse(new TeacherMyClassesDtos.LessonNoteResponse(null, null, null));
    }

    @Override
    @Transactional
    public TeacherMyClassesDtos.LessonNoteResponse saveLessonNotes(Long facultyId, TeacherLessonNoteRequest request) {
        Faculty faculty = loadFaculty(facultyId);
        ResolvedSession rs = resolveSessionOrThrow(facultyId, request.getSessionKind(), request.getRefId(), request.getSessionDate());
        TeacherSessionNote note = teacherSessionNoteRepository
                .findByFaculty_IdAndSessionKindAndRefIdAndSessionDate(
                        facultyId, request.getSessionKind(), request.getRefId(), request.getSessionDate())
                .orElse(TeacherSessionNote.builder()
                        .faculty(faculty)
                        .schoolId(faculty.getSchool().getId())
                        .sessionDate(rs.date())
                        .sessionKind(request.getSessionKind())
                        .refId(request.getRefId())
                        .className(rs.className())
                        .section(rs.section())
                        .subject(rs.subject())
                        .sessionStartTime(rs.start())
                        .sessionEndTime(rs.end())
                        .build());
        note.setTopicCovered(trimToNull(request.getTopicCovered()));
        note.setDescriptionNotes(trimToNull(request.getDescriptionNotes()));
        note.setHomework(trimToNull(request.getHomework()));
        note.setSessionStartTime(rs.start());
        note.setSessionEndTime(rs.end());
        note.setClassName(rs.className());
        note.setSection(rs.section());
        note.setSubject(rs.subject());
        teacherSessionNoteRepository.save(note);
        return new TeacherMyClassesDtos.LessonNoteResponse(
                note.getTopicCovered(), note.getDescriptionNotes(), note.getHomework());
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherMyClassesDtos.AttendanceRosterResponse getAttendanceRoster(
            Long facultyId, String sessionKind, Long refId, LocalDate sessionDate) {
        Faculty faculty = loadFaculty(facultyId);
        ResolvedSession rs = resolveSessionOrThrow(facultyId, sessionKind, refId, sessionDate);
        List<Student> students = studentRepository.findBySchool_IdAndClassNameAndSection(
                faculty.getSchool().getId(), rs.className(), rs.section());
        students.sort(Comparator.comparing(Student::getRollNo));
        List<Long> ids = students.stream().map(Student::getId).toList();
        Map<Long, AttendanceStatus> statusByStudent = new HashMap<>();
        if (!ids.isEmpty()) {
            for (AttendanceRecord ar : attendanceRecordRepository.findByDateAndStudent_IdIn(sessionDate, ids)) {
                statusByStudent.put(ar.getStudent().getId(), ar.getStatus());
            }
        }
        long p = 0, a = 0, l = 0, lv = 0, nm = 0;
        List<TeacherMyClassesDtos.AttendanceRosterRow> rows = new ArrayList<>();
        for (Student s : students) {
            AttendanceStatus st = statusByStudent.get(s.getId());
            String label = st == null ? "NOT_MARKED" : st.name();
            if (st == null) {
                nm++;
            } else {
                switch (st) {
                    case PRESENT -> p++;
                    case ABSENT -> a++;
                    case LATE -> l++;
                    case LEAVE -> lv++;
                }
            }
            rows.add(new TeacherMyClassesDtos.AttendanceRosterRow(
                    s.getId(),
                    s.getRollNo(),
                    (s.getFirstName() + " " + s.getLastName()).trim(),
                    label
            ));
        }
        return new TeacherMyClassesDtos.AttendanceRosterResponse(
                sessionDate, rs.className(), rs.section(), p, a, l, lv, nm, rows);
    }

    @Override
    @Transactional
    public void submitSessionAttendance(Long facultyId, TeacherSessionAttendanceSubmitRequest request) {
        Faculty faculty = loadFaculty(facultyId);
        ResolvedSession rs = resolveSessionOrThrow(facultyId, request.getSessionKind(), request.getRefId(), request.getSessionDate());
        for (TeacherSessionAttendanceSubmitRequest.Row row : request.getRows()) {
            AttendanceRequest ar = AttendanceRequest.builder()
                    .studentId(row.getStudentId())
                    .date(request.getSessionDate())
                    .status(row.getStatus())
                    .className(rs.className())
                    .section(rs.section())
                    .subjectId(rs.subject() != null ? rs.subject().getId() : null)
                    .remarks(row.getRemarks())
                    .build();
            markAttendanceMerge(ar, faculty.getSchool().getId(), rs.className(), rs.section());
        }
    }

    private void markAttendanceMerge(AttendanceRequest request, Long schoolId, String className, String section) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (!student.getSchool().getId().equals(schoolId)
                || !student.getClassName().equalsIgnoreCase(className)
                || !student.getSection().equalsIgnoreCase(section)) {
            throw new IllegalArgumentException("Student is not in this class");
        }
        AttendanceRecord existing = attendanceRecordRepository.findByDateAndStudentId(request.getDate(), request.getStudentId());
        if (existing != null) {
            existing.setStatus(request.getStatus());
            existing.setRemarks(request.getRemarks());
            existing.setClassName(className);
            existing.setSection(section);
            attendanceRecordRepository.save(existing);
        } else {
            attendanceRecordRepository.save(AttendanceRecord.builder()
                    .student(student)
                    .date(request.getDate())
                    .status(request.getStatus())
                    .className(className)
                    .section(section)
                    .remarks(request.getRemarks())
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherMyClassesDtos.ImportAttendancePreviewResponse previewAttendanceImport(
            Long facultyId, String sessionKind, Long refId, LocalDate sessionDate, MultipartFile file) {
        ResolvedSession rs = resolveSessionOrThrow(facultyId, sessionKind, refId, sessionDate);
        Faculty faculty = loadFaculty(facultyId);
        List<Student> students = studentRepository.findBySchool_IdAndClassNameAndSection(
                faculty.getSchool().getId(), rs.className(), rs.section());
        Map<Integer, Student> byRoll = students.stream().collect(Collectors.toMap(Student::getRollNo, s -> s, (a, b) -> a));

        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        List<TeacherMyClassesDtos.ImportAttendancePreviewRow> rows = new ArrayList<>();

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
                        rows.add(new TeacherMyClassesDtos.ImportAttendancePreviewRow(
                                roll,
                                stu.getId(),
                                (stu.getFirstName() + " " + stu.getLastName()).trim(),
                                suggested,
                                0.95
                        ));
                    } catch (NumberFormatException ignored) {
                        // header row
                    }
                }
            } catch (Exception e) {
                return new TeacherMyClassesDtos.ImportAttendancePreviewResponse(
                        List.of(), "Could not read CSV: " + e.getMessage());
            }
            return new TeacherMyClassesDtos.ImportAttendancePreviewResponse(
                    rows, rows.isEmpty() ? "No rows parsed. Use columns: roll,status (PRESENT/ABSENT/LATE/LEAVE)." : "Parsed from CSV.");
        }

        return new TeacherMyClassesDtos.ImportAttendancePreviewResponse(
                List.of(),
                "OCR / image import is not configured. Upload a CSV with columns: roll number, status (PRESENT, ABSENT, LATE, LEAVE).");
    }

    @Override
    @Transactional
    public TeacherMyClassesDtos.AssignmentPublishedResponse publishSessionAssignment(
            Long facultyId,
            String sessionKind,
            Long refId,
            LocalDate sessionDate,
            String title,
            String description,
            LocalDate dueDate,
            MultipartFile attachment) {
        Faculty faculty = loadFaculty(facultyId);
        ResolvedSession rs = resolveSessionOrThrow(facultyId, sessionKind, refId, sessionDate);
        if (rs.subject() == null) {
            throw new IllegalArgumentException("Session has no subject; cannot publish assignment.");
        }
        String attachmentPath = null;
        if (attachment != null && !attachment.isEmpty()) {
            attachmentPath = storeAttachment(facultyId, attachment);
        }
        Assignment a = Assignment.builder()
                .subject(rs.subject())
                .teacher(faculty)
                .schoolId(faculty.getSchool().getId())
                .className(rs.className())
                .section(rs.section())
                .title(title.trim())
                .description(description)
                .dueDate(dueDate)
                .attachmentPath(attachmentPath)
                .publishStatus(com.org.careerbuilder.models.enums.AssignmentPublishStatus.PUBLISHED)
                .build();
        Assignment saved = assignmentRepository.save(a);
        return new TeacherMyClassesDtos.AssignmentPublishedResponse(saved.getId(), saved.getTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public Resource exportTeachingLog(Long facultyId, String academicYear) {
        TeacherMyClassesDtos.TeachingLogTableResponse table = buildTeachingLog(facultyId, academicYear);
        StringBuilder sb = new StringBuilder("date,time,class,section,subject,status,takenAs\n");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (TeacherMyClassesDtos.TeachingLogRow r : table.rows()) {
            sb.append(r.date().format(df)).append(',')
                    .append(r.timeLabel()).append(',')
                    .append(csvEscape(r.className())).append(',')
                    .append(csvEscape(r.section())).append(',')
                    .append(csvEscape(r.subjectName())).append(',')
                    .append(csvEscape(r.status())).append(',')
                    .append(csvEscape(r.takenAs())).append('\n');
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return new ByteArrayResource(bytes);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherMyClassesDtos.AcademicYearsResponse listAcademicYears() {
        int y = LocalDate.now().getYear();
        List<String> list = new ArrayList<>();
        for (int i = -1; i <= 2; i++) {
            int yy = y + i;
            list.add(yy + "-" + String.format("%02d", (yy + 1) % 100));
        }
        return new TeacherMyClassesDtos.AcademicYearsResponse(list);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherMyClassesDtos.TeacherPortalSearchResponse search(Long facultyId, String q) {
        if (q == null || q.isBlank()) {
            return new TeacherMyClassesDtos.TeacherPortalSearchResponse(List.of(), List.of(), List.of());
        }
        Faculty faculty = loadFaculty(facultyId);
        Long schoolId = faculty.getSchool().getId();
        String needle = q.trim();
        String needleLower = needle.toLowerCase(Locale.ROOT);

        Set<String> allowed = classSubjectTeacherRepository.findByFaculty_IdAndActiveTrue(facultyId).stream()
                .map(c -> c.getClassName().toLowerCase(Locale.ROOT) + "|" + c.getSection().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        List<TeacherMyClassesDtos.SearchHitClass> classes = classSubjectTeacherRepository.findByFaculty_IdAndActiveTrue(facultyId).stream()
                .map(c -> new TeacherMyClassesDtos.SearchHitClass(
                        c.getClassName(),
                        c.getSection(),
                        "Grade " + c.getClassName() + c.getSection()))
                .filter(c -> c.label().toLowerCase(Locale.ROOT).contains(needleLower)
                        || c.className().toLowerCase(Locale.ROOT).contains(needleLower))
                .distinct()
                .limit(15)
                .toList();

        List<Student> rawStudents = studentRepository.searchBySchoolText(schoolId, needle);
        List<TeacherMyClassesDtos.SearchHitStudent> students = rawStudents.stream()
                .filter(s -> allowed.contains(s.getClassName().toLowerCase(Locale.ROOT) + "|" + s.getSection().toLowerCase(Locale.ROOT)))
                .limit(20)
                .map(s -> new TeacherMyClassesDtos.SearchHitStudent(
                        s.getId(),
                        (s.getFirstName() + " " + s.getLastName()).trim(),
                        s.getRollNo(),
                        s.getClassName(),
                        s.getSection()
                ))
                .toList();

        List<TeacherNotice> notices = teacherNoticeRepository.searchByFaculty(
                facultyId, needle, PageRequest.of(0, 15));
        List<TeacherMyClassesDtos.SearchHitNotice> noticeHits = notices.stream()
                .map(n -> new TeacherMyClassesDtos.SearchHitNotice(
                        n.getId(),
                        n.getTitle(),
                        n.getDescription().length() > 120 ? n.getDescription().substring(0, 117) + "..." : n.getDescription()
                ))
                .toList();

        return new TeacherMyClassesDtos.TeacherPortalSearchResponse(students, classes, noticeHits);
    }

    // --- internals ---

    private TeacherMyClassesDtos.TeachingLogTableResponse buildTeachingLog(Long facultyId, String academicYear) {
        TeacherMyClassesDtos.ClassSessionListResponse list =
                listSessions(facultyId, "history", academicYear, null);
        List<TeacherMyClassesDtos.TeachingLogRow> rows = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (TeacherMyClassesDtos.ClassSessionCard c : list.sessions()) {
            LocalDateTime end = LocalDateTime.of(c.sessionDate(), c.endTime());
            String status = now.isAfter(end) ? "COMPLETED" : "SCHEDULED";
            String takenAs = c.extraSession() ? "EXTRA"
                    : (c.substituteForDisplay() != null && !c.substituteForDisplay().isBlank() ? "SUBSTITUTE" : "REGULAR");
            rows.add(new TeacherMyClassesDtos.TeachingLogRow(
                    c.sessionDate(),
                    c.startTime(),
                    TIME_FMT_12.format(c.startTime()),
                    c.gradeDisplay(),
                    c.className(),
                    c.section(),
                    c.subjectName(),
                    status,
                    takenAs
            ));
        }
        return new TeacherMyClassesDtos.TeachingLogTableResponse(rows);
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

    private String storeAttachment(Long facultyId, MultipartFile file) {
        try {
            Path dir = Paths.get("uploads", "teacher-session-assignments", String.valueOf(facultyId));
            Files.createDirectories(dir);
            String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String safe = original.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = dir.resolve(UUID.randomUUID() + "_" + safe);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString().replace("\\", "/");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store attachment", e);
        }
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
        if (st.contains("PRES")) {
            return "PRESENT";
        }
        return "PRESENT";
    }

    private Optional<TeacherMyClassesDtos.ClassSessionCard> mapItemToCard(Faculty faculty, TeacherScheduleDtos.ScheduleItemResponse item, LocalDate d) {
        if ("CLASS_TIMETABLE".equals(item.source())) {
            if (!item.showViewClass()) {
                return Optional.empty();
            }
            Optional<ClassScheduleSlot> slotOpt = classScheduleSlotRepository.findById(item.sourceId());
            if (slotOpt.isEmpty()) {
                return Optional.empty();
            }
            ClassScheduleSlot slot = slotOpt.get();
            assertFacultyTeaches(faculty.getId(), faculty.getSchool().getId(), item.className(), item.section(), slot.getSubject().getId());
            return Optional.of(buildCardFromSlot(faculty, slot, item, d));
        }
        if ("TEACHER_ENTRY".equals(item.source())) {
            Optional<TeacherScheduleEntry> entryOpt = teacherScheduleEntryRepository.findById(item.sourceId());
            if (entryOpt.isEmpty()) {
                return Optional.empty();
            }
            TeacherScheduleEntry entry = entryOpt.get();
            if (entry.getActivityType() != TeacherActivityType.CLASS
                    || entry.getClassName() == null || entry.getClassName().isBlank()) {
                return Optional.empty();
            }
            assertFacultyTeaches(faculty.getId(), faculty.getSchool().getId(), entry.getClassName(), entry.getSection(),
                    entry.getSubject() != null ? entry.getSubject().getId() : null);
            return Optional.of(buildCardFromEntry(faculty, entry, item, d));
        }
        return Optional.empty();
    }

    private TeacherMyClassesDtos.ClassSessionCard buildCardFromSlot(Faculty faculty, ClassScheduleSlot slot, TeacherScheduleDtos.ScheduleItemResponse item, LocalDate d) {
        String last = lastTopic(faculty.getId(), item.className(), item.section(), slot.getSubject().getId(), d, slot.getStartTime());
        SessionStatusModel st = computeCardStatus(faculty.getSchool().getId(), item.className(), item.section(), d, item.startTime(), item.endTime());
        int studentCount = (int) studentRepository.countBySchool_IdAndClassNameAndSection(
                faculty.getSchool().getId(), item.className(), item.section());
        return new TeacherMyClassesDtos.ClassSessionCard(
                KIND_TIMETABLE_SLOT,
                slot.getId(),
                d,
                item.startTime(),
                item.endTime(),
                timeRange(item.startTime(), item.endTime()),
                gradeDisplay(item.className(), item.section()),
                item.className(),
                item.section(),
                slot.getSubject().getId(),
                slot.getSubject().getName(),
                last,
                st.label(),
                st.attendancePending(),
                false,
                null,
                item.source(),
                studentCount
        );
    }

    private TeacherMyClassesDtos.ClassSessionCard buildCardFromEntry(Faculty faculty, TeacherScheduleEntry entry, TeacherScheduleDtos.ScheduleItemResponse item, LocalDate d) {
        Subject subj = entry.getSubject();
        Long sid = subj != null ? subj.getId() : null;
        String sname = subj != null ? subj.getName() : item.title();
        String last = sid != null
                ? lastTopic(faculty.getId(), entry.getClassName(), entry.getSection(), sid, d, item.startTime())
                : "";
        boolean extra = !entry.isRecurring() && entry.getSpecificDate() != null && entry.getActivityType() == TeacherActivityType.CLASS;
        String subLabel = substituteLabel(entry.getSubstituteForName());
        SessionStatusModel st = computeCardStatus(faculty.getSchool().getId(), entry.getClassName(), entry.getSection(), d, item.startTime(), item.endTime());
        int studentCount = (int) studentRepository.countBySchool_IdAndClassNameAndSection(
                faculty.getSchool().getId(), entry.getClassName(), entry.getSection());
        return new TeacherMyClassesDtos.ClassSessionCard(
                KIND_TEACHER_ENTRY,
                entry.getId(),
                d,
                item.startTime(),
                item.endTime(),
                timeRange(item.startTime(), item.endTime()),
                gradeDisplay(entry.getClassName(), entry.getSection()),
                entry.getClassName(),
                entry.getSection(),
                sid,
                sname,
                last,
                st.label(),
                st.attendancePending(),
                extra,
                subLabel,
                item.source(),
                studentCount
        );
    }

    private record SessionStatusModel(String label, boolean attendancePending) {}

    private SessionStatusModel computeCardStatus(Long schoolId, String className, String section, LocalDate d, LocalTime start, LocalTime end) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sessionStart = d.atTime(start);
        LocalDateTime sessionEnd = d.atTime(end);
        int n = (int) studentRepository.countBySchool_IdAndClassNameAndSection(schoolId, className, section);
        boolean allMarked = n == 0 || allStudentsMarked(schoolId, className, section, d, n);
        if (now.isBefore(sessionStart)) {
            return new SessionStatusModel("UPCOMING", false);
        }
        if (!allMarked && !now.isAfter(sessionEnd)) {
            return new SessionStatusModel("ATTENDANCE_PENDING", true);
        }
        if (!allMarked) {
            return new SessionStatusModel("ATTENDANCE_PENDING", true);
        }
        return new SessionStatusModel("COMPLETED", false);
    }

    private boolean allStudentsMarked(Long schoolId, String className, String section, LocalDate date, int expectedCount) {
        List<Student> students = studentRepository.findBySchool_IdAndClassNameAndSection(schoolId, className, section);
        if (students.isEmpty()) {
            return true;
        }
        List<Long> ids = students.stream().map(Student::getId).toList();
        List<AttendanceRecord> recs = attendanceRecordRepository.findByDateAndStudent_IdIn(date, ids);
        return recs.size() >= expectedCount;
    }

    private String lastTopic(Long facultyId, String className, String section, Long subjectId, LocalDate sessionDate, LocalTime sessionStart) {
        List<TeacherSessionNote> prev = teacherSessionNoteRepository.findRecentTopicsBefore(
                facultyId, className, section, subjectId, sessionDate, sessionStart, PageRequest.of(0, 1));
        return prev.isEmpty() ? "" : Optional.ofNullable(prev.get(0).getTopicCovered()).orElse("");
    }

    private void assertFacultyTeaches(Long facultyId, Long schoolId, String className, String section, Long subjectId) {
        List<ClassSubjectTeacher> list = subjectId == null
                ? classSubjectTeacherRepository.findBySchoolIdAndClassNameAndSectionAndActiveTrue(schoolId, className, section)
                : classSubjectTeacherRepository.findBySchoolIdAndClassNameAndSectionAndSubject_IdAndActiveTrue(
                        schoolId, className, section, subjectId);
        boolean ok = list.stream().anyMatch(c -> c.getFaculty().getId().equals(facultyId));
        if (!ok) {
            throw new IllegalArgumentException("Teacher is not assigned to this class/subject");
        }
    }

    private ResolvedSession resolveSessionOrThrow(Long facultyId, String sessionKind, Long refId, LocalDate sessionDate) {
        Faculty faculty = loadFaculty(facultyId);
        if (KIND_TIMETABLE_SLOT.equals(sessionKind)) {
            ClassScheduleSlot slot = classScheduleSlotRepository.findById(refId)
                    .orElseThrow(() -> new ResourceNotFoundException("Timetable slot not found"));
            assertFacultyTeaches(facultyId, faculty.getSchool().getId(), slot.getClassName(), slot.getSection(), slot.getSubject().getId());
            int dow = sessionDate.getDayOfWeek().getValue();
            if (slot.getDayOfWeek() != dow) {
                throw new IllegalArgumentException("Session date does not match timetable weekday for this slot");
            }
            String sub = substituteLabel(null);
            return new ResolvedSession(
                    faculty.getSchool().getId(),
                    slot.getClassName(),
                    slot.getSection(),
                    slot.getSubject(),
                    slot.getStartTime(),
                    slot.getEndTime(),
                    sessionDate,
                    KIND_TIMETABLE_SLOT,
                    slot.getId(),
                    false,
                    sub
            );
        }
        if (KIND_TEACHER_ENTRY.equals(sessionKind)) {
            TeacherScheduleEntry e = teacherScheduleEntryRepository.findById(refId)
                    .orElseThrow(() -> new ResourceNotFoundException("Schedule entry not found"));
            if (!e.getFaculty().getId().equals(facultyId)) {
                throw new IllegalArgumentException("Session belongs to another teacher");
            }
            boolean applies = (!e.isRecurring() && sessionDate.equals(e.getSpecificDate()))
                    || (e.isRecurring() && e.getDayOfWeek() != null && e.getDayOfWeek() == sessionDate.getDayOfWeek().getValue());
            if (!applies) {
                throw new IllegalArgumentException("Session date does not apply to this schedule entry");
            }
            boolean extra = !e.isRecurring() && e.getSpecificDate() != null && e.getActivityType() == TeacherActivityType.CLASS;
            if (e.getClassName() != null && e.getSubject() != null) {
                assertFacultyTeaches(facultyId, faculty.getSchool().getId(), e.getClassName(), e.getSection(), e.getSubject().getId());
            }
            return new ResolvedSession(
                    faculty.getSchool().getId(),
                    e.getClassName(),
                    e.getSection(),
                    e.getSubject(),
                    e.getStartTime(),
                    e.getEndTime(),
                    sessionDate,
                    KIND_TEACHER_ENTRY,
                    e.getId(),
                    extra,
                    substituteLabel(e.getSubstituteForName())
            );
        }
        throw new IllegalArgumentException("Unknown sessionKind");
    }

    private record ResolvedSession(
            Long schoolId,
            String className,
            String section,
            Subject subject,
            LocalTime start,
            LocalTime end,
            LocalDate date,
            String kind,
            Long refId,
            boolean extraSession,
            String substituteForDisplay
    ) {}

    private Faculty loadFaculty(Long facultyId) {
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
    }

    private static String normalizeRange(String timeRange) {
        if (timeRange == null || timeRange.isBlank()) {
            return "today";
        }
        return timeRange.trim().toLowerCase(Locale.ROOT);
    }

    private static List<LocalDate> datesForRange(String timeRange, LocalDate today) {
        return switch (timeRange) {
            case "today" -> List.of(today);
            case "tomorrow" -> List.of(today.plusDays(1));
            case "week" -> {
                LocalDate mon = today.minusDays((today.getDayOfWeek().getValue() + 6) % 7);
                List<LocalDate> d = new ArrayList<>();
                for (int i = 0; i < 7; i++) {
                    d.add(mon.plusDays(i));
                }
                yield d;
            }
            case "month" -> {
                LocalDate first = today.withDayOfMonth(1);
                LocalDate last = first.plusMonths(1).minusDays(1);
                List<LocalDate> d = new ArrayList<>();
                for (LocalDate x = first; !x.isAfter(last); x = x.plusDays(1)) {
                    d.add(x);
                }
                yield d;
            }
            case "history" -> {
                List<LocalDate> d = new ArrayList<>();
                for (int i = 1; i <= 120; i++) {
                    d.add(today.minusDays(i));
                }
                yield d;
            }
            default -> throw new IllegalArgumentException("timeRange must be today|tomorrow|week|month|history");
        };
    }

    /**
     * Academic year "2026-27" → 1 June 2026 – 31 May 2027 (inclusive).
     */
    private static LocalDate[] parseAcademicYear(String academicYear) {
        if (academicYear == null || academicYear.isBlank()) {
            return null;
        }
        String cleaned = academicYear.trim().replace("AY", "").replace("ay", "").trim();
        String[] parts = cleaned.split("-");
        if (parts.length < 2) {
            return null;
        }
        try {
            int y1 = Integer.parseInt(parts[0].trim());
            String p2 = parts[1].trim();
            int y2;
            if (p2.length() <= 2) {
                int y2Short = Integer.parseInt(p2);
                y2 = y1 / 100 * 100 + y2Short;
                if (y2 < y1) {
                    y2 += 100;
                }
            } else {
                y2 = Integer.parseInt(p2);
            }
            LocalDate start = LocalDate.of(y1, 6, 1);
            LocalDate end = LocalDate.of(y2, 5, 31);
            return new LocalDate[]{start, end};
        } catch (Exception e) {
            return null;
        }
    }

    private static String gradeDisplay(String className, String section) {
        if (className == null) {
            return "";
        }
        String c = className.trim();
        String s = section == null ? "" : section.trim();
        if (c.toLowerCase(Locale.ROOT).startsWith("grade")) {
            return c + s;
        }
        return "Grade " + c + s;
    }

    private static String timeRange(LocalTime start, LocalTime end) {
        return TIME_FMT.format(start) + " - " + TIME_FMT.format(end);
    }

    private static String substituteLabel(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return "HANDLING FOR " + raw.trim().toUpperCase(Locale.ROOT);
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
