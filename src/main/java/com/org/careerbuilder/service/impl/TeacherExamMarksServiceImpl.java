package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.ExamMarkEntryBatchRequest;
import com.org.careerbuilder.dto.request.ExamMarkEntryRequest;
import com.org.careerbuilder.dto.request.ExamMarksEditRequest;
import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.Exam;
import com.org.careerbuilder.models.ExamResult;
import com.org.careerbuilder.models.Faculty;
import com.org.careerbuilder.models.Subject;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.models.enums.ExamType;
import com.org.careerbuilder.models.ClassSubjectTeacher;
import com.org.careerbuilder.repository.ClassSubjectTeacherRepository;
import com.org.careerbuilder.repository.ExamResultRepository;
import com.org.careerbuilder.repository.FacultyRepository;
import com.org.careerbuilder.service.TeacherExamMarksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherExamMarksServiceImpl implements TeacherExamMarksService {

    private static final long MAX_IMPORT_BYTES = 10L * 1024 * 1024;

    private final ExamResultRepository examResultRepository;
    private final FacultyRepository facultyRepository;
    private final ClassSubjectTeacherRepository classSubjectTeacherRepository;

    private static int passingMarksFor(int totalMarks) {
        if (totalMarks <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(totalMarks * 0.5));
    }

    private static String formatTime(Exam exam) {
        if (exam.getStartTime() == null) {
            return null;
        }
        return exam.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    private Faculty loadFaculty(Long facultyId) {
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
    }

    private Set<Long> taughtSubjectIds(Long facultyId) {
        return classSubjectTeacherRepository.findByFaculty_IdAndActiveTrueWithSubject(facultyId).stream()
                .map(ClassSubjectTeacher::getSubject)
                .filter(Objects::nonNull)
                .map(Subject::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void assertFacultyOwnsExam(Long facultyId, Faculty faculty, Exam exam) {
        if (exam.getSubject() == null) {
            throw new IllegalArgumentException("This exam is not for your subject");
        }
        Set<Long> taught = taughtSubjectIds(facultyId);
        if (!taught.isEmpty()) {
            if (!taught.contains(exam.getSubject().getId())) {
                throw new IllegalArgumentException("This exam is not for your subject");
            }
            return;
        }
        if (!exam.getSubject().getId().equals(faculty.getSubject().getId())) {
            throw new IllegalArgumentException("This exam is not for your subject");
        }
    }

    private void assertStudentInFacultySchool(Faculty faculty, Student student) {
        if (student.getSchool() == null || !student.getSchool().getId().equals(faculty.getSchool().getId())) {
            throw new IllegalArgumentException("Student is outside your school");
        }
    }

    private Long resolveSubjectFilter(Long facultyId, Faculty faculty, Long subjectId) {
        Set<Long> taught = taughtSubjectIds(facultyId);
        if (subjectId == null) {
            if (!taught.isEmpty()) {
                return taught.iterator().next();
            }
            return faculty.getSubject().getId();
        }
        if (!taught.isEmpty() && !taught.contains(subjectId)) {
            throw new IllegalArgumentException("You can only access your own subject");
        }
        if (taught.isEmpty() && !subjectId.equals(faculty.getSubject().getId())) {
            throw new IllegalArgumentException("You can only access your own subject");
        }
        return subjectId;
    }

    private List<ExamResult> findTeacherExamResultsForFaculty(
            Faculty faculty,
            Long facultyId,
            Long subjectId,
            String className,
            String section,
            ExamType examType) {
        Set<Long> taught = taughtSubjectIds(facultyId);
        if (subjectId != null) {
            return examResultRepository.findTeacherExamResults(
                    faculty.getSchool().getId(), subjectId, className, section, examType);
        }
        if (taught.isEmpty()) {
            return examResultRepository.findTeacherExamResults(
                    faculty.getSchool().getId(),
                    faculty.getSubject().getId(),
                    className,
                    section,
                    examType);
        }
        List<ExamResult> merged = new ArrayList<>();
        for (Long subId : taught) {
            merged.addAll(examResultRepository.findTeacherExamResults(
                    faculty.getSchool().getId(), subId, className, section, examType));
        }
        return merged;
    }

    private ExamType parseExamType(String examType) {
        if (examType == null || examType.isBlank()) {
            return null;
        }
        try {
            return ExamType.valueOf(examType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid examType. Allowed: INTERNAL, WEEKLY, MID_TERM, FINAL");
        }
    }

    private String normalize(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }

    private String rowStatus(ExamResult result, int passingMarks) {
        if (result.getObtainedMarks() == null) {
            return "PENDING";
        }
        return result.getObtainedMarks() >= passingMarks ? "PASS" : "FAIL";
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherExamFiltersResponse getExamFilters(Long facultyId) {
        Faculty faculty = loadFaculty(facultyId);
        List<ClassSubjectTeacher> assignments =
                classSubjectTeacherRepository.findByFaculty_IdAndActiveTrueWithSubject(facultyId);

        Map<String, TeacherExamFiltersResponse.ClassSectionOption> classKeys = new LinkedHashMap<>();
        Map<Long, TeacherExamFiltersResponse.SubjectOption> subjectKeys = new LinkedHashMap<>();
        for (ClassSubjectTeacher cst : assignments) {
            if (cst.getClassName() != null && cst.getSection() != null) {
                String key = cst.getClassName() + "|" + cst.getSection();
                classKeys.putIfAbsent(key, TeacherExamFiltersResponse.ClassSectionOption.builder()
                        .className(cst.getClassName())
                        .section(cst.getSection())
                        .label("Grade " + cst.getClassName() + " " + cst.getSection())
                        .build());
            }
            if (cst.getSubject() != null) {
                subjectKeys.putIfAbsent(cst.getSubject().getId(),
                        TeacherExamFiltersResponse.SubjectOption.builder()
                                .id(cst.getSubject().getId())
                                .name(cst.getSubject().getName())
                                .build());
            }
        }

        if (subjectKeys.isEmpty() && faculty.getSubject() != null) {
            subjectKeys.put(faculty.getSubject().getId(),
                    TeacherExamFiltersResponse.SubjectOption.builder()
                            .id(faculty.getSubject().getId())
                            .name(faculty.getSubject().getName())
                            .build());
        }

        List<String> types = Arrays.stream(ExamType.values()).map(Enum::name).toList();
        List<String> statuses = List.of("MARKS_PENDING", "UPCOMING", "SUBMITTED");

        return TeacherExamFiltersResponse.builder()
                .classes(new ArrayList<>(classKeys.values()))
                .subjects(new ArrayList<>(subjectKeys.values()))
                .examTypes(types)
                .statuses(statuses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherExamCardResponse> listExamsForMarks(
            Long facultyId,
            Boolean history,
            String className,
            String section,
            String examType,
            String status,
            Long subjectId) {
        Faculty faculty = loadFaculty(facultyId);
        if (faculty.getSubject() == null && taughtSubjectIds(facultyId).isEmpty()) {
            return List.of();
        }
        Long subId = subjectId != null ? resolveSubjectFilter(facultyId, faculty, subjectId) : null;
        ExamType typeFilter = parseExamType(examType);

        List<ExamResult> rows = findTeacherExamResultsForFaculty(
                faculty,
                facultyId,
                subId,
                normalize(className),
                normalize(section),
                typeFilter);

        Map<Long, List<ExamResult>> byExam = rows.stream().collect(Collectors.groupingBy(er -> er.getExam().getId()));

        boolean wantHistory = Boolean.TRUE.equals(history);

        return byExam.values().stream()
                .map(list -> toExamCard(list, faculty))
                .filter(card -> {
                    if (wantHistory) {
                        return "SUBMITTED".equalsIgnoreCase(card.getStatus());
                    }
                    return "MARKS_PENDING".equalsIgnoreCase(card.getStatus())
                            || "UPCOMING".equalsIgnoreCase(card.getStatus());
                })
                .filter(card -> {
                    if (status == null || status.isBlank()) {
                        return true;
                    }
                    return card.getStatus() != null && card.getStatus().equalsIgnoreCase(status.trim());
                })
                .sorted(Comparator.comparing(TeacherExamCardResponse::getExamDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(TeacherExamCardResponse::getTitle, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    private TeacherExamCardResponse toExamCard(List<ExamResult> rows, Faculty faculty) {
        ExamResult first = rows.get(0);
        Exam exam = first.getExam();
        Student st = first.getStudent();
        int totalMarks = rows.stream().map(ExamResult::getTotalMarks).filter(Objects::nonNull).findFirst().orElse(0);
        int passing = passingMarksFor(totalMarks);
        String cardStatus = resolveCardStatus(exam, rows);

        String venue = exam.getVenue();
        return TeacherExamCardResponse.builder()
                .id(exam.getId())
                .title(exam.getName())
                .examType(exam.getExamType() != null ? exam.getExamType().name() : null)
                .className(st.getClassName())
                .section(st.getSection())
                .subjectName(exam.getSubject() != null ? exam.getSubject().getName() : null)
                .examDate(exam.getExamDate())
                .time(formatTime(exam))
                .venue(venue)
                .location(venue)
                .status(cardStatus)
                .totalMarks(totalMarks)
                .passingMarks(passing)
                .build();
    }

    private String resolveCardStatus(Exam exam, List<ExamResult> rows) {
        if (exam.getExamDate() != null && exam.getExamDate().isAfter(LocalDate.now())) {
            return "UPCOMING";
        }
        boolean allLocked = !rows.isEmpty() && rows.stream().allMatch(ExamResult::isMarksLocked);
        return allLocked ? "SUBMITTED" : "MARKS_PENDING";
    }

    private List<ExamResult> loadExamBoardRows(Long facultyId, Long examId) {
        Faculty faculty = loadFaculty(facultyId);
        List<ExamResult> results = examResultRepository.findWithContextByExamId(examId);
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("No exam found with id: " + examId);
        }
        assertFacultyOwnsExam(facultyId, faculty, results.get(0).getExam());
        for (ExamResult r : results) {
            assertStudentInFacultySchool(faculty, r.getStudent());
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public ExamMarkEntryBoardResponse getMarkEntryBoard(Long facultyId, Long examId) {
        List<ExamResult> results = loadExamBoardRows(facultyId, examId);
        return buildMarkEntryBoard(results.get(0), results);
    }

    @Override
    @Transactional
    public ExamMarkEntryBoardResponse saveMarks(Long facultyId, Long examId, ExamMarkEntryBatchRequest request) {
        if (!examId.equals(request.getExamId())) {
            throw new IllegalArgumentException("examId in path and body must match");
        }
        Faculty faculty = loadFaculty(facultyId);
        List<ExamResult> board = loadExamBoardRows(facultyId, examId);

        Map<Long, ExamResult> byId = board.stream().collect(Collectors.toMap(ExamResult::getId, r -> r));

        for (ExamMarkEntryRequest markReq : request.getMarks()) {
            ExamResult result = byId.get(markReq.getResultId());
            if (result == null) {
                throw new IllegalArgumentException("Invalid resultId for this exam: " + markReq.getResultId());
            }
            if (result.isMarksLocked()) {
                throw new IllegalStateException("Marks are locked; cannot save draft");
            }
            if (markReq.getMarks() != null) {
                int m = (int) Math.round(markReq.getMarks());
                if (m < 0 || m > result.getTotalMarks()) {
                    throw new IllegalArgumentException("Marks must be between 0 and " + result.getTotalMarks());
                }
                result.setObtainedMarks(m);
            }
            if (markReq.getRemark() != null) {
                result.setRemarks(markReq.getRemark());
            }
            examResultRepository.save(result);
        }

        List<ExamResult> refreshed = examResultRepository.findWithContextByExamId(examId);
        return buildMarkEntryBoard(refreshed.get(0), refreshed);
    }

    @Override
    @Transactional
    public ExamMarkEntryRowResponse updateMark(Long facultyId, Long examId, Long resultId, ExamMarkEntryRequest request) {
        Faculty faculty = loadFaculty(facultyId);
        ExamResult result = examResultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("ExamResult not found"));
        if (!result.getExam().getId().equals(examId)) {
            throw new IllegalArgumentException("Result does not belong to this exam");
        }
        assertFacultyOwnsExam(facultyId, faculty, result.getExam());
        assertStudentInFacultySchool(faculty, result.getStudent());
        if (result.isMarksLocked()) {
            throw new IllegalStateException("Marks are locked");
        }
        if (request.getMarks() != null) {
            int m = (int) Math.round(request.getMarks());
            if (m < 0 || m > result.getTotalMarks()) {
                throw new IllegalArgumentException("Marks must be between 0 and " + result.getTotalMarks());
            }
            result.setObtainedMarks(m);
        }
        if (request.getRemark() != null) {
            result.setRemarks(request.getRemark());
        }
        ExamResult saved = examResultRepository.save(result);
        return mapToMarkEntryRow(saved);
    }

    @Override
    @Transactional
    public ExamMarkEntryBoardResponse submitAndLockMarks(Long facultyId, Long examId) {
        List<ExamResult> results = loadExamBoardRows(facultyId, examId);

        LocalDateTime now = LocalDateTime.now();
        for (ExamResult result : results) {
            result.setMarksLocked(true);
            result.setMarksLockedAt(now);
            examResultRepository.save(result);
        }

        List<ExamResult> refreshed = examResultRepository.findWithContextByExamId(examId);
        return buildMarkEntryBoard(refreshed.get(0), refreshed);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamMarksHistoryResponse> getMarksHistory(Long facultyId, String className, String section) {
        Faculty faculty = loadFaculty(facultyId);
        Set<Long> taught = taughtSubjectIds(facultyId);
        List<ExamResult> locked = new ArrayList<>();
        if (taught.isEmpty()) {
            locked.addAll(examResultRepository.findLockedTeacherExamResults(
                    faculty.getSchool().getId(),
                    faculty.getSubject().getId(),
                    normalize(className),
                    normalize(section)));
        } else {
            for (Long subId : taught) {
                locked.addAll(examResultRepository.findLockedTeacherExamResults(
                        faculty.getSchool().getId(),
                        subId,
                        normalize(className),
                        normalize(section)));
            }
        }

        Map<Long, List<ExamResult>> byExam = locked.stream().collect(Collectors.groupingBy(er -> er.getExam().getId()));

        return byExam.values().stream()
                .map(this::toMarksHistoryRow)
                .sorted(Comparator.comparing(ExamMarksHistoryResponse::getSubmittedOn,
                                Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    private ExamMarksHistoryResponse toMarksHistoryRow(List<ExamResult> rows) {
        ExamResult any = rows.get(0);
        Exam exam = any.getExam();
        Student st = any.getStudent();
        int totalMarks = rows.stream().map(ExamResult::getTotalMarks).filter(Objects::nonNull).findFirst().orElse(0);
        int passCut = passingMarksFor(totalMarks);

        List<ExamResult> scored = rows.stream().filter(r -> r.getObtainedMarks() != null).toList();
        double avg = scored.isEmpty() ? 0.0
                : scored.stream().mapToInt(ExamResult::getObtainedMarks).average().orElse(0.0);
        int pass = (int) scored.stream().filter(r -> r.getObtainedMarks() >= passCut).count();
        int fail = (int) scored.stream().filter(r -> r.getObtainedMarks() < passCut).count();

        LocalDateTime submitted = rows.stream()
                .map(ExamResult::getMarksLockedAt)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return ExamMarksHistoryResponse.builder()
                .examId(exam.getId())
                .examTitle(exam.getName())
                .className(st.getClassName())
                .section(st.getSection())
                .subjectName(exam.getSubject() != null ? exam.getSubject().getName() : null)
                .examDate(exam.getExamDate())
                .venue(exam.getVenue())
                .submittedOn(submitted)
                .totalStudents(rows.size())
                .presentCount(scored.size())
                .averageMarks(Math.round(avg * 10.0) / 10.0)
                .passCount(pass)
                .failCount(fail)
                .marksStatus("LOCKED")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ExamMarkEntryBoardResponse getMarksDetail(Long facultyId, Long examId) {
        return getMarkEntryBoard(facultyId, examId);
    }

    @Override
    @Transactional
    public ExamMarkEntryRowResponse editLockedMarks(Long facultyId, Long examId, ExamMarksEditRequest request) {
        Faculty faculty = loadFaculty(facultyId);
        ExamResult result = examResultRepository.findById(request.getResultId())
                .orElseThrow(() -> new ResourceNotFoundException("ExamResult not found"));
        if (!result.getExam().getId().equals(examId)) {
            throw new IllegalArgumentException("Result does not belong to this exam");
        }
        assertFacultyOwnsExam(facultyId, faculty, result.getExam());
        assertStudentInFacultySchool(faculty, result.getStudent());
        if (!result.isMarksLocked()) {
            throw new IllegalStateException("Marks are not locked for this result");
        }
        int m = (int) Math.round(request.getMarks());
        if (m < 0 || m > result.getTotalMarks()) {
            throw new IllegalArgumentException("Marks must be between 0 and " + result.getTotalMarks());
        }
        result.setObtainedMarks(m);
        result.setRemarks(request.getRemark());
        result.setMarksEditedAt(LocalDateTime.now());
        result.setMarksEditReason(request.getReasonForEdit());
        result.setEditedByTeacherId(facultyId);
        ExamResult saved = examResultRepository.save(result);
        return mapToMarkEntryRow(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ImportMarksPreviewResponse previewImportMarks(Long facultyId, Long examId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        if (file.getSize() > MAX_IMPORT_BYTES) {
            throw new IllegalArgumentException("File exceeds 10MB limit");
        }
        loadExamBoardRows(facultyId, examId);
        Map<Integer, ExamResult> byRoll = rollMapForExam(examId);

        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";
        try {
            if (name.endsWith(".csv")) {
                return parseImportCsv(file, byRoll);
            }
            return parseImportExcel(file, byRoll);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse file", e);
        }
    }

    private Map<Integer, ExamResult> rollMapForExam(Long examId) {
        List<ExamResult> board = examResultRepository.findWithContextByExamId(examId);
        Map<Integer, List<ExamResult>> grouped = board.stream().collect(Collectors.groupingBy(r -> r.getStudent().getRollNo()));
        Map<Integer, ExamResult> map = new HashMap<>();
        for (Map.Entry<Integer, List<ExamResult>> e : grouped.entrySet()) {
            if (e.getValue().size() == 1) {
                map.put(e.getKey(), e.getValue().get(0));
            }
        }
        return map;
    }

    private ImportMarksPreviewResponse parseImportCsv(MultipartFile file, Map<Integer, ExamResult> byRoll) throws IOException {
        List<ImportMarksPreviewResponse.ImportedMarkRow> rows = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int valid = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                return ImportMarksPreviewResponse.builder()
                        .totalRows(0).validRows(0).invalidRows(0).rows(List.of()).errors(List.of("Empty file")).build();
            }
            String[] headers = headerLine.split(",");
            int rollCol = -1;
            int marksCol = -1;
            for (int i = 0; i < headers.length; i++) {
                String h = headers[i].trim().toLowerCase(Locale.ROOT);
                if (h.contains("roll")) {
                    rollCol = i;
                }
                if (h.contains("mark")) {
                    marksCol = i;
                }
            }
            if (rollCol < 0 || marksCol < 0) {
                throw new IllegalArgumentException("CSV must contain Roll and Marks columns in the header row");
            }

            String line;
            int lineNo = 1;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",");
                try {
                    if (parts.length <= Math.max(rollCol, marksCol)) {
                        throw new IllegalArgumentException("Not enough columns");
                    }
                    int roll = Integer.parseInt(parts[rollCol].trim());
                    double marks = Double.parseDouble(parts[marksCol].trim());
                    ExamResult target = byRoll.get(roll);
                    if (target == null) {
                        throw new IllegalArgumentException("Roll " + roll + " not in this exam roster (or duplicate roll in class)");
                    }
                    if (marks < 0 || marks > target.getTotalMarks()) {
                        throw new IllegalArgumentException("Marks out of range (0-" + target.getTotalMarks() + ")");
                    }
                    rows.add(ImportMarksPreviewResponse.ImportedMarkRow.builder()
                            .lineNumber(lineNo)
                            .rollNo(roll)
                            .studentName(target.getStudent().getFirstName() + " " + target.getStudent().getLastName())
                            .marks(marks)
                            .isValid(true)
                            .build());
                    valid++;
                } catch (Exception ex) {
                    errors.add("Row " + lineNo + ": " + ex.getMessage());
                    rows.add(ImportMarksPreviewResponse.ImportedMarkRow.builder()
                            .lineNumber(lineNo)
                            .isValid(false)
                            .errorMessage(ex.getMessage())
                            .build());
                }
            }

            return ImportMarksPreviewResponse.builder()
                    .totalRows(lineNo - 1)
                    .validRows(valid)
                    .invalidRows(rows.size() - valid)
                    .rows(rows)
                    .errors(errors)
                    .build();
        }
    }

    private ImportMarksPreviewResponse parseImportExcel(MultipartFile file, Map<Integer, ExamResult> byRoll) throws IOException {
        List<ImportMarksPreviewResponse.ImportedMarkRow> rows = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int valid = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                return ImportMarksPreviewResponse.builder()
                        .totalRows(0).validRows(0).invalidRows(0).rows(List.of()).errors(List.of("No sheet")).build();
            }

            Row header = sheet.getRow(0);
            if (header == null) {
                throw new IllegalArgumentException("Missing header row");
            }
            int rollCol = -1;
            int marksCol = -1;
            for (int c = 0; c < header.getLastCellNum(); c++) {
                String h = cellString(header.getCell(c)).toLowerCase(Locale.ROOT);
                if (h.contains("roll")) {
                    rollCol = c;
                }
                if (h.contains("mark")) {
                    marksCol = c;
                }
            }
            if (rollCol < 0 || marksCol < 0) {
                throw new IllegalArgumentException("Sheet must contain Roll and Marks columns in the header row");
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                int lineNo = i + 1;
                try {
                    String rollStr = cellString(row.getCell(rollCol));
                    if (rollStr.isEmpty()) {
                        continue;
                    }
                    int roll = Integer.parseInt(rollStr);
                    String markStr = cellString(row.getCell(marksCol));
                    if (markStr.isEmpty()) {
                        continue;
                    }
                    double marks = Double.parseDouble(markStr);
                    ExamResult target = byRoll.get(roll);
                    if (target == null) {
                        throw new IllegalArgumentException("Roll " + roll + " not in this exam roster (or duplicate roll in class)");
                    }
                    if (marks < 0 || marks > target.getTotalMarks()) {
                        throw new IllegalArgumentException("Marks out of range (0-" + target.getTotalMarks() + ")");
                    }
                    rows.add(ImportMarksPreviewResponse.ImportedMarkRow.builder()
                            .lineNumber(lineNo)
                            .rollNo(roll)
                            .studentName(target.getStudent().getFirstName() + " " + target.getStudent().getLastName())
                            .marks(marks)
                            .isValid(true)
                            .build());
                    valid++;
                } catch (Exception ex) {
                    errors.add("Row " + lineNo + ": " + ex.getMessage());
                    rows.add(ImportMarksPreviewResponse.ImportedMarkRow.builder()
                            .lineNumber(lineNo)
                            .isValid(false)
                            .errorMessage(ex.getMessage())
                            .build());
                }
            }

            return ImportMarksPreviewResponse.builder()
                    .totalRows(sheet.getLastRowNum())
                    .validRows(valid)
                    .invalidRows(rows.size() - valid)
                    .rows(rows)
                    .errors(errors)
                    .build();
        }
    }

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    private static String cellString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return DATA_FORMATTER.formatCellValue(cell).trim();
    }

    @Override
    @Transactional
    public ExamMarkEntryBoardResponse confirmImportMarks(Long facultyId, Long examId, List<ImportMarksPreviewResponse.ImportedMarkRow> rows) {
        Faculty faculty = loadFaculty(facultyId);
        loadExamBoardRows(facultyId, examId);
        Map<Integer, ExamResult> byRoll = rollMapForExam(examId);

        for (ImportMarksPreviewResponse.ImportedMarkRow row : rows) {
            if (!Boolean.TRUE.equals(row.getIsValid()) || row.getRollNo() == null || row.getMarks() == null) {
                continue;
            }
            ExamResult result = byRoll.get(row.getRollNo());
            if (result == null || result.isMarksLocked()) {
                continue;
            }
            assertFacultyOwnsExam(facultyId, faculty, result.getExam());
            int m = (int) Math.round(row.getMarks());
            if (m >= 0 && m <= result.getTotalMarks()) {
                result.setObtainedMarks(m);
                examResultRepository.save(result);
            }
        }

        List<ExamResult> refreshed = examResultRepository.findWithContextByExamId(examId);
        return buildMarkEntryBoard(refreshed.get(0), refreshed);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportMarks(Long facultyId, Long examId) {
        loadExamBoardRows(facultyId, examId);
        List<ExamResult> results = examResultRepository.findWithContextByExamId(examId);
        int passCut = results.isEmpty() ? 0 : passingMarksFor(results.get(0).getTotalMarks());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Marks");
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Roll No", "Student Name", "Marks", "Remark", "Status"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            int rowNum = 1;
            for (ExamResult result : results) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(result.getStudent().getRollNo());
                row.createCell(1).setCellValue(result.getStudent().getFirstName() + " " + result.getStudent().getLastName());
                if (result.getObtainedMarks() != null) {
                    row.createCell(2).setCellValue(result.getObtainedMarks());
                } else {
                    row.createCell(2).setCellValue("");
                }
                row.createCell(3).setCellValue(result.getRemarks() != null ? result.getRemarks() : "");
                String st = rowStatus(result, passCut);
                row.createCell(4).setCellValue("PENDING".equals(st) ? "-" : st);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to export marks", ex);
        }
    }

    private ExamMarkEntryBoardResponse buildMarkEntryBoard(ExamResult first, List<ExamResult> results) {
        Exam exam = first.getExam();
        Student st = first.getStudent();
        int totalMarks = results.stream().map(ExamResult::getTotalMarks).filter(Objects::nonNull).findFirst().orElse(0);
        int passing = passingMarksFor(totalMarks);
        boolean locked = !results.isEmpty() && results.stream().allMatch(ExamResult::isMarksLocked);
        List<ExamMarkEntryRowResponse> rows = results.stream()
                .map(r -> mapToMarkEntryRow(r, passing))
                .toList();
        long entered = results.stream().filter(r -> r.getObtainedMarks() != null).count();

        return ExamMarkEntryBoardResponse.builder()
                .examId(exam.getId())
                .examTitle(exam.getName())
                .className(st.getClassName())
                .section(st.getSection())
                .subjectName(exam.getSubject() != null ? exam.getSubject().getName() : null)
                .examDate(exam.getExamDate())
                .venue(exam.getVenue())
                .totalMarks(totalMarks)
                .passingMarks(passing)
                .totalStudents(results.size())
                .presentCount((int) entered)
                .students(rows)
                .isLocked(locked)
                .status(locked ? "SUBMITTED" : "IN_PROGRESS")
                .build();
    }

    private ExamMarkEntryRowResponse mapToMarkEntryRow(ExamResult result) {
        int passing = passingMarksFor(result.getTotalMarks());
        return mapToMarkEntryRow(result, passing);
    }

    private ExamMarkEntryRowResponse mapToMarkEntryRow(ExamResult result, int passingMarks) {
        Student student = result.getStudent();
        return ExamMarkEntryRowResponse.builder()
                .resultId(result.getId())
                .studentId(student.getId())
                .rollNo(student.getRollNo())
                .studentName(student.getFirstName() + " " + student.getLastName())
                .marks(result.getObtainedMarks() != null ? result.getObtainedMarks().doubleValue() : null)
                .remark(result.getRemarks())
                .status(rowStatus(result, passingMarks))
                .build();
    }
}
