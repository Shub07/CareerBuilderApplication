package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.TeacherQuizReviewSaveRequest;
import com.org.careerbuilder.dto.request.TeacherQuizUpsertRequest;
import com.org.careerbuilder.dto.response.TeacherQuizDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.QuizLifecycleStatus;
import com.org.careerbuilder.models.enums.QuizQuestionType;
import com.org.careerbuilder.models.enums.QuizSubmissionGradeStatus;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.TeacherQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherQuizServiceImpl implements TeacherQuizService {

    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final ClassSubjectTeacherRepository classSubjectTeacherRepository;
    private final StudentRepository studentRepository;
    private final TeacherQuizRepository teacherQuizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;

    @Override
    @Transactional(readOnly = true)
    public TeacherQuizDtos.QuizFiltersResponse getFilters(Long facultyId) {
        loadFaculty(facultyId);
        List<ClassSubjectTeacher> csts = classSubjectTeacherRepository.findByFaculty_IdAndActiveTrue(facultyId);
        Map<String, TeacherQuizDtos.ClassSectionOption> classes = new LinkedHashMap<>();
        Map<Long, TeacherQuizDtos.SubjectOption> subjects = new LinkedHashMap<>();
        for (ClassSubjectTeacher cst : csts) {
            String key = cst.getClassName().toLowerCase(Locale.ROOT) + "|" + cst.getSection().toLowerCase(Locale.ROOT);
            classes.putIfAbsent(key, new TeacherQuizDtos.ClassSectionOption(
                    cst.getClassName(), cst.getSection(),
                    "Grade " + cst.getClassName() + " " + cst.getSection()));
            if (cst.getSubject() != null) {
                subjects.putIfAbsent(cst.getSubject().getId(),
                        new TeacherQuizDtos.SubjectOption(cst.getSubject().getId(), cst.getSubject().getName()));
            }
        }
        return new TeacherQuizDtos.QuizFiltersResponse(new ArrayList<>(classes.values()), new ArrayList<>(subjects.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherQuizDtos.QuizListPage listQuizzes(
            Long facultyId,
            String view,
            String className,
            String section,
            Long subjectId,
            String lifecycleStatus,
            LocalDate dateFrom,
            LocalDate dateTo,
            String search,
            Pageable pageable) {
        loadFaculty(facultyId);
        Specification<TeacherQuiz> spec = Specification.where(teacherEq(facultyId)).and(tabSpec(view));
        if (className != null && !className.isBlank()) {
            spec = spec.and((r, q, cb) -> cb.equal(cb.lower(r.get("className")), className.trim().toLowerCase(Locale.ROOT)));
        }
        if (section != null && !section.isBlank()) {
            spec = spec.and((r, q, cb) -> cb.equal(cb.lower(r.get("section")), section.trim().toLowerCase(Locale.ROOT)));
        }
        if (subjectId != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("subject").get("id"), subjectId));
        }
        if (lifecycleStatus != null && !lifecycleStatus.isBlank()) {
            final QuizLifecycleStatus st;
            try {
                st = QuizLifecycleStatus.valueOf(lifecycleStatus.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid lifecycleStatus: " + lifecycleStatus);
            }
            spec = spec.and((r, q, cb) -> cb.equal(r.get("lifecycleStatus"), st));
        }
        if (dateFrom != null) {
            spec = spec.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("createdAt"), dateFrom.atStartOfDay()));
        }
        if (dateTo != null) {
            spec = spec.and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("createdAt"), dateTo.atTime(23, 59, 59)));
        }
        if (search != null && !search.isBlank()) {
            String p = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((r, q, cb) -> cb.like(cb.lower(r.get("title")), p));
        }

        Page<TeacherQuiz> page = teacherQuizRepository.findAll(spec, pageable);
        List<TeacherQuizDtos.QuizCardRow> rows = new ArrayList<>();
        for (TeacherQuiz qz : page.getContent()) {
            int qCount = (int) quizQuestionRepository.countByQuiz_Id(qz.getId());
            int totalStudents = (int) studentRepository.countBySchool_IdAndClassNameAndSection(
                    qz.getSchoolId(), qz.getClassName(), qz.getSection());
            int attempts = (int) quizSubmissionRepository.countByQuiz_IdAndSubmittedAtIsNotNull(qz.getId());
            Double avg = quizSubmissionRepository.averageTotalScore(qz.getId(), QuizSubmissionGradeStatus.CHECKED)
                    .orElse(null);
            rows.add(new TeacherQuizDtos.QuizCardRow(
                    qz.getId(),
                    qz.getTitle(),
                    safeSubjectName(qz),
                    classLabel(qz),
                    qz.getLifecycleStatus().name(),
                    qCount,
                    qz.getTimeLimitMinutes(),
                    attempts,
                    totalStudents,
                    avg,
                    qz.getTotalMarks()
            ));
        }
        return new TeacherQuizDtos.QuizListPage(rows, page.getTotalPages(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherQuizDtos.QuizDetailResponse getQuizDetail(Long facultyId, Long quizId) {
        TeacherQuiz qz = loadQuizForFacultyWithQuestions(facultyId, quizId);
        return toDetailResponse(qz);
    }

    @Override
    @Transactional
    public Long createQuiz(Long facultyId, TeacherQuizUpsertRequest request) {
        Faculty faculty = loadFaculty(facultyId);
        assertTeaches(facultyId, faculty.getSchool().getId(), request.getClassName(), request.getSection(), request.getSubjectId());
        BigDecimal sumMarks = validateAndSumQuestions(request);
        int totalMarks = resolveTotalMarks(request, sumMarks);
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        QuizLifecycleStatus ls = request.getLifecycleStatus() != null ? request.getLifecycleStatus() : QuizLifecycleStatus.DRAFT;
        TeacherQuiz quiz = TeacherQuiz.builder()
                .schoolId(faculty.getSchool().getId())
                .teacher(faculty)
                .subject(subject)
                .title(request.getTitle().trim())
                .instructions(request.getInstructions())
                .className(request.getClassName().trim())
                .section(request.getSection().trim())
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .totalMarks(totalMarks)
                .scheduledAt(request.getScheduledAt())
                .shuffleQuestions(Boolean.TRUE.equals(request.getShuffleQuestions()))
                .lifecycleStatus(ls)
                .build();
        attachQuestionsFromRequest(quiz, request);
        return teacherQuizRepository.save(quiz).getId();
    }

    @Override
    @Transactional
    public void updateQuiz(Long facultyId, Long quizId, TeacherQuizUpsertRequest request) {
        TeacherQuiz quiz = loadQuizForFacultyWithQuestions(facultyId, quizId);
        if (quizSubmissionRepository.countByQuiz_IdAndSubmittedAtIsNotNull(quizId) > 0) {
            throw new IllegalArgumentException("Cannot replace quiz questions after students have submitted attempts");
        }
        assertTeaches(facultyId, quiz.getSchoolId(), request.getClassName(), request.getSection(), request.getSubjectId());
        BigDecimal sumMarks = validateAndSumQuestions(request);
        int totalMarks = resolveTotalMarks(request, sumMarks);
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        quiz.setTitle(request.getTitle().trim());
        quiz.setInstructions(request.getInstructions());
        quiz.setClassName(request.getClassName().trim());
        quiz.setSection(request.getSection().trim());
        quiz.setSubject(subject);
        quiz.setTimeLimitMinutes(request.getTimeLimitMinutes());
        quiz.setTotalMarks(totalMarks);
        quiz.setScheduledAt(request.getScheduledAt());
        quiz.setShuffleQuestions(Boolean.TRUE.equals(request.getShuffleQuestions()));
        if (request.getLifecycleStatus() != null) {
            quiz.setLifecycleStatus(request.getLifecycleStatus());
        }
        quiz.getQuestions().clear();
        attachQuestionsFromRequest(quiz, request);
        teacherQuizRepository.save(quiz);
    }

    @Override
    @Transactional
    public void deleteQuiz(Long facultyId, Long quizId) {
        loadQuizForFaculty(facultyId, quizId);
        quizSubmissionRepository.deleteByQuiz_Id(quizId);
        teacherQuizRepository.deleteById(quizId);
    }

    @Override
    @Transactional
    public TeacherQuizDtos.DuplicateQuizResponse duplicateQuiz(Long facultyId, Long quizId) {
        TeacherQuiz src = loadQuizForFacultyWithQuestions(facultyId, quizId);
        String newTitle = truncate("Copy of " + src.getTitle(), 300);
        TeacherQuiz copy = TeacherQuiz.builder()
                .schoolId(src.getSchoolId())
                .teacher(src.getTeacher())
                .subject(src.getSubject())
                .title(newTitle)
                .instructions(src.getInstructions())
                .className(src.getClassName())
                .section(src.getSection())
                .timeLimitMinutes(src.getTimeLimitMinutes())
                .totalMarks(src.getTotalMarks())
                .scheduledAt(src.getScheduledAt())
                .shuffleQuestions(src.isShuffleQuestions())
                .lifecycleStatus(QuizLifecycleStatus.DRAFT)
                .conductedOn(null)
                .build();
        for (QuizQuestion qq : src.getQuestions()) {
            QuizQuestion nq = QuizQuestion.builder()
                    .quiz(copy)
                    .sortOrder(qq.getSortOrder())
                    .questionType(qq.getQuestionType())
                    .questionText(qq.getQuestionText())
                    .maxMarks(qq.getMaxMarks())
                    .answerKey(qq.getAnswerKey())
                    .build();
            for (QuizQuestionOption o : qq.getOptions()) {
                QuizQuestionOption no = QuizQuestionOption.builder()
                        .question(nq)
                        .sortOrder(o.getSortOrder())
                        .optionText(o.getOptionText())
                        .correct(o.isCorrect())
                        .build();
                nq.getOptions().add(no);
            }
            copy.getQuestions().add(nq);
        }
        return new TeacherQuizDtos.DuplicateQuizResponse(teacherQuizRepository.save(copy).getId());
    }

    @Override
    @Transactional
    public void publishQuiz(Long facultyId, Long quizId) {
        TeacherQuiz qz = loadQuizForFaculty(facultyId, quizId);
        qz.setLifecycleStatus(QuizLifecycleStatus.PUBLISHED);
        teacherQuizRepository.save(qz);
    }

    @Override
    @Transactional
    public void markConducted(Long facultyId, Long quizId) {
        TeacherQuiz qz = loadQuizForFaculty(facultyId, quizId);
        qz.setLifecycleStatus(QuizLifecycleStatus.CONDUCTED);
        qz.setConductedOn(LocalDate.now());
        teacherQuizRepository.save(qz);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherQuizDtos.QuizResultsPage getResults(Long facultyId, Long quizId, String search, Pageable pageable) {
        TeacherQuiz qz = loadQuizForFaculty(facultyId, quizId);
        List<Student> students = studentRepository.findBySchool_IdAndClassNameAndSection(
                qz.getSchoolId(), qz.getClassName(), qz.getSection());
        students.sort(Comparator.comparing(Student::getRollNo));
        Map<Long, QuizSubmission> byStudent = quizSubmissionRepository.findByQuiz_Id(quizId).stream()
                .collect(Collectors.toMap(s -> s.getStudent().getId(), s -> s, (a, b) -> a));

        int totalStudents = students.size();
        int attempted = (int) quizSubmissionRepository.countByQuiz_IdAndSubmittedAtIsNotNull(quizId);
        int pendingReview = (int) quizSubmissionRepository.countByQuiz_IdAndSubmittedAtIsNotNullAndGradeStatus(
                quizId, QuizSubmissionGradeStatus.PENDING);
        BigDecimal avg = quizSubmissionRepository.averageTotalScore(quizId, QuizSubmissionGradeStatus.CHECKED)
                .map(BigDecimal::valueOf)
                .map(x -> x.setScale(2, RoundingMode.HALF_UP))
                .orElse(null);

        TeacherQuizDtos.QuizResultsSummary summary = new TeacherQuizDtos.QuizResultsSummary(
                attempted, totalStudents, pendingReview, avg, qz.getTotalMarks());

        String q = search == null || search.isBlank() ? null : search.trim().toLowerCase(Locale.ROOT);
        List<TeacherQuizDtos.QuizResultStudentRow> all = new ArrayList<>();
        for (Student st : students) {
            String name = (st.getFirstName() + " " + st.getLastName()).trim();
            if (q != null && !name.toLowerCase(Locale.ROOT).contains(q)) {
                continue;
            }
            QuizSubmission sub = byStudent.get(st.getId());
            all.add(toResultRow(st, sub, qz.getTotalMarks()));
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<TeacherQuizDtos.QuizResultStudentRow> slice = start >= all.size() ? List.of() : all.subList(start, end);
        int totalPages = (int) Math.ceil(all.size() / (double) pageable.getPageSize());
        return new TeacherQuizDtos.QuizResultsPage(summary, slice, totalPages, all.size());
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherQuizDtos.QuizSubmissionReviewResponse getSubmissionReview(Long facultyId, Long quizId, Long submissionId) {
        TeacherQuiz qz = loadQuizForFacultyWithQuestions(facultyId, quizId);
        QuizSubmission sub = quizSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        if (!sub.getQuiz().getId().equals(quizId)) {
            throw new IllegalArgumentException("Submission does not belong to this quiz");
        }
        Student st = sub.getStudent();
        String studentName = (st.getFirstName() + " " + st.getLastName()).trim();
        sub.getAnswers().size(); // initialize lazy collection in read transaction
        Map<Long, QuizSubmissionAnswer> byQuestion = sub.getAnswers().stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a, (x, y) -> x));

        List<QuizQuestion> ordered = new ArrayList<>(qz.getQuestions());
        ordered.sort(Comparator.comparing(QuizQuestion::getSortOrder).thenComparing(QuizQuestion::getId));
        List<TeacherQuizDtos.QuizAnswerReviewLine> lines = new ArrayList<>();
        for (QuizQuestion question : ordered) {
            QuizSubmissionAnswer ans = byQuestion.get(question.getId());
            String answerText = ans != null ? ans.getAnswerText() : null;
            Long selId = ans != null && ans.getSelectedOption() != null ? ans.getSelectedOption().getId() : null;
            String selText = ans != null && ans.getSelectedOption() != null ? ans.getSelectedOption().getOptionText() : null;
            BigDecimal awarded = ans != null ? ans.getMarksAwarded() : null;
            lines.add(new TeacherQuizDtos.QuizAnswerReviewLine(
                    question.getId(),
                    question.getSortOrder(),
                    question.getQuestionType().name(),
                    question.getQuestionText(),
                    question.getMaxMarks(),
                    answerText,
                    selId,
                    selText,
                    awarded
            ));
        }
        BigDecimal current = sub.getTotalScore() != null ? sub.getTotalScore() : sumAwarded(sub);
        return new TeacherQuizDtos.QuizSubmissionReviewResponse(
                sub.getId(),
                qz.getId(),
                st.getId(),
                studentName,
                current,
                qz.getTotalMarks(),
                sub.getTeacherRemark(),
                sub.getGradeStatus().name(),
                lines
        );
    }

    @Override
    @Transactional
    public void saveSubmissionReview(Long facultyId, Long quizId, Long submissionId, TeacherQuizReviewSaveRequest request) {
        TeacherQuiz qz = loadQuizForFacultyWithQuestions(facultyId, quizId);
        QuizSubmission sub = quizSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        if (!sub.getQuiz().getId().equals(quizId)) {
            throw new IllegalArgumentException("Submission does not belong to this quiz");
        }
        ensureSubmissionAnswers(sub, qz);
        Map<Long, QuizSubmissionAnswer> byQuestion = sub.getAnswers().stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a, (x, y) -> x));
        Map<Long, QuizQuestion> questionsById = qz.getQuestions().stream()
                .collect(Collectors.toMap(QuizQuestion::getId, qq -> qq, (a, b) -> a));

        if (request.answers() != null) {
            for (TeacherQuizReviewSaveRequest.AnswerMark am : request.answers()) {
                QuizQuestion qq = questionsById.get(am.questionId());
                if (qq == null) {
                    throw new IllegalArgumentException("Unknown question id: " + am.questionId());
                }
                QuizSubmissionAnswer row = byQuestion.get(am.questionId());
                if (row == null) {
                    row = QuizSubmissionAnswer.builder()
                            .submission(sub)
                            .question(qq)
                            .build();
                    sub.getAnswers().add(row);
                    byQuestion.put(qq.getId(), row);
                }
                if (am.marksAwarded() != null) {
                    if (am.marksAwarded().compareTo(BigDecimal.ZERO) < 0
                            || am.marksAwarded().compareTo(qq.getMaxMarks()) > 0) {
                        throw new IllegalArgumentException("Marks out of range for question " + am.questionId());
                    }
                    row.setMarksAwarded(am.marksAwarded());
                }
            }
        }
        if (request.teacherRemark() != null) {
            sub.setTeacherRemark(request.teacherRemark());
        }
        sub.setTotalScore(sumAwarded(sub));
        sub.setGradeStatus(QuizSubmissionGradeStatus.CHECKED);
        quizSubmissionRepository.save(sub);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource exportResultsCsv(Long facultyId, Long quizId) {
        TeacherQuiz qz = loadQuizForFaculty(facultyId, quizId);
        List<Student> students = studentRepository.findBySchool_IdAndClassNameAndSection(
                qz.getSchoolId(), qz.getClassName(), qz.getSection());
        students.sort(Comparator.comparing(Student::getRollNo));
        Map<Long, QuizSubmission> byStudent = quizSubmissionRepository.findByQuiz_Id(quizId).stream()
                .collect(Collectors.toMap(s -> s.getStudent().getId(), s -> s, (a, b) -> a));

        StringBuilder sb = new StringBuilder();
        sb.append("Student Name,Roll,Score,Percentage,Status\n");
        for (Student st : students) {
            QuizSubmission sub = byStudent.get(st.getId());
            TeacherQuizDtos.QuizResultStudentRow row = toResultRow(st, sub, qz.getTotalMarks());
            String score = row.scoreObtained() != null ? row.scoreObtained().toPlainString() : "";
            String pct = row.percentage() != null ? row.percentage().toPlainString() : "";
            sb.append(csvEscape(row.displayName())).append(',')
                    .append(row.rollNo() != null ? row.rollNo() : "").append(',')
                    .append(csvEscape(score)).append(',')
                    .append(csvEscape(pct)).append(',')
                    .append(row.gradeStatus())
                    .append('\n');
        }
        return new ByteArrayResource(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String csvEscape(String s) {
        if (s == null || s.isEmpty()) {
            return s == null ? "" : s;
        }
        String x = s.replace("\"", "\"\"");
        if (x.contains(",") || x.contains("\"") || x.contains("\n")) {
            return "\"" + x + "\"";
        }
        return x;
    }

    private void ensureSubmissionAnswers(QuizSubmission sub, TeacherQuiz qz) {
        Map<Long, QuizSubmissionAnswer> byQ = sub.getAnswers().stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a, (x, y) -> x));
        for (QuizQuestion question : qz.getQuestions()) {
            if (!byQ.containsKey(question.getId())) {
                QuizSubmissionAnswer a = QuizSubmissionAnswer.builder()
                        .submission(sub)
                        .question(question)
                        .build();
                sub.getAnswers().add(a);
            }
        }
    }

    private static BigDecimal sumAwarded(QuizSubmission sub) {
        return sub.getAnswers().stream()
                .map(QuizSubmissionAnswer::getMarksAwarded)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private TeacherQuizDtos.QuizResultStudentRow toResultRow(Student st, QuizSubmission sub, int totalMarks) {
        String name = (st.getFirstName() + " " + st.getLastName()).trim();
        String initials = initials(name);
        if (sub == null || sub.getSubmittedAt() == null) {
            return new TeacherQuizDtos.QuizResultStudentRow(
                    sub != null ? sub.getId() : null,
                    st.getId(),
                    name,
                    st.getRollNo(),
                    initials,
                    null,
                    totalMarks,
                    null,
                    "PENDING");
        }
        BigDecimal pct = null;
        if (sub.getTotalScore() != null && totalMarks > 0) {
            pct = sub.getTotalScore()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalMarks), 2, RoundingMode.HALF_UP);
        }
        String status = sub.getGradeStatus() == QuizSubmissionGradeStatus.CHECKED ? "CHECKED" : "PENDING";
        return new TeacherQuizDtos.QuizResultStudentRow(
                sub.getId(),
                st.getId(),
                name,
                st.getRollNo(),
                initials,
                sub.getTotalScore(),
                totalMarks,
                pct,
                status
        );
    }

    private static String initials(String name) {
        String[] p = name.trim().split("\\s+");
        if (p.length == 0) {
            return "?";
        }
        if (p.length == 1) {
            return p[0].substring(0, Math.min(2, p[0].length())).toUpperCase(Locale.ROOT);
        }
        return (p[0].substring(0, 1) + p[p.length - 1].substring(0, 1)).toUpperCase(Locale.ROOT);
    }

    private TeacherQuizDtos.QuizDetailResponse toDetailResponse(TeacherQuiz qz) {
        List<QuizQuestion> ordered = new ArrayList<>(qz.getQuestions());
        ordered.sort(Comparator.comparing(QuizQuestion::getSortOrder).thenComparing(QuizQuestion::getId));
        List<TeacherQuizDtos.QuizQuestionResponse> qrs = new ArrayList<>();
        for (QuizQuestion qq : ordered) {
            List<TeacherQuizDtos.QuizOptionResponse> opts = qq.getOptions().stream()
                    .sorted(Comparator.comparing(QuizQuestionOption::getSortOrder).thenComparing(QuizQuestionOption::getId))
                    .map(o -> new TeacherQuizDtos.QuizOptionResponse(o.getId(), o.getSortOrder(), o.getOptionText(), o.isCorrect()))
                    .toList();
            qrs.add(new TeacherQuizDtos.QuizQuestionResponse(
                    qq.getId(),
                    qq.getSortOrder(),
                    qq.getQuestionType().name(),
                    qq.getQuestionText(),
                    qq.getMaxMarks(),
                    qq.getAnswerKey(),
                    opts
            ));
        }
        return new TeacherQuizDtos.QuizDetailResponse(
                qz.getId(),
                qz.getTitle(),
                qz.getInstructions(),
                qz.getClassName(),
                qz.getSection(),
                classLabel(qz),
                qz.getSubject().getId(),
                safeSubjectName(qz),
                qz.getTimeLimitMinutes(),
                qz.getTotalMarks(),
                qz.getScheduledAt(),
                qz.isShuffleQuestions(),
                qz.getLifecycleStatus().name(),
                qz.getConductedOn(),
                qrs
        );
    }

    private void attachQuestionsFromRequest(TeacherQuiz quiz, TeacherQuizUpsertRequest request) {
        for (TeacherQuizUpsertRequest.TeacherQuizQuestionRequest qr : request.getQuestions()) {
            validateQuestionRequest(qr);
            QuizQuestion qq = QuizQuestion.builder()
                    .quiz(quiz)
                    .sortOrder(qr.getSortOrder())
                    .questionType(qr.getQuestionType())
                    .questionText(qr.getQuestionText().trim())
                    .maxMarks(qr.getMaxMarks())
                    .answerKey(qr.getAnswerKey())
                    .build();
            if (qr.getQuestionType() == QuizQuestionType.MCQ && qr.getOptions() != null) {
                for (TeacherQuizUpsertRequest.TeacherQuizOptionRequest or : qr.getOptions()) {
                    QuizQuestionOption oo = QuizQuestionOption.builder()
                            .question(qq)
                            .sortOrder(or.getSortOrder())
                            .optionText(or.getOptionText().trim())
                            .correct(Boolean.TRUE.equals(or.getCorrect()))
                            .build();
                    qq.getOptions().add(oo);
                }
            }
            quiz.getQuestions().add(qq);
        }
    }

    private void validateQuestionRequest(TeacherQuizUpsertRequest.TeacherQuizQuestionRequest qr) {
        if (qr.getQuestionType() == QuizQuestionType.MCQ) {
            if (qr.getOptions() == null || qr.getOptions().size() < 2) {
                throw new IllegalArgumentException("Each MCQ needs at least two options");
            }
            long correct = qr.getOptions().stream().filter(o -> Boolean.TRUE.equals(o.getCorrect())).count();
            if (correct != 1) {
                throw new IllegalArgumentException("Each MCQ must have exactly one correct option");
            }
        } else if (qr.getOptions() != null && !qr.getOptions().isEmpty()) {
            throw new IllegalArgumentException("Short-answer questions must not include options");
        }
    }

    private BigDecimal validateAndSumQuestions(TeacherQuizUpsertRequest request) {
        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("At least one question is required");
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (TeacherQuizUpsertRequest.TeacherQuizQuestionRequest qr : request.getQuestions()) {
            validateQuestionRequest(qr);
            sum = sum.add(qr.getMaxMarks());
        }
        return sum;
    }

    private int resolveTotalMarks(TeacherQuizUpsertRequest request, BigDecimal sumMarks) {
        int computed = sumMarks.setScale(0, RoundingMode.HALF_UP).intValue();
        if (computed < 1) {
            throw new IllegalArgumentException("Total marks must be at least 1");
        }
        if (request.getTotalMarks() != null && request.getTotalMarks() != computed) {
            throw new IllegalArgumentException("totalMarks must equal the rounded sum of question max marks (" + computed + ")");
        }
        return computed;
    }

    private static Specification<TeacherQuiz> teacherEq(Long facultyId) {
        return (r, q, cb) -> cb.equal(r.get("teacher").get("id"), facultyId);
    }

    private static Specification<TeacherQuiz> tabSpec(String view) {
        String v = view == null || view.isBlank() ? "active" : view.trim().toLowerCase(Locale.ROOT);
        LocalDate monthStart = YearMonth.now().atDay(1);
        if ("history".equals(v)) {
            return (r, q, cb) -> cb.and(
                    cb.equal(r.get("lifecycleStatus"), QuizLifecycleStatus.CONDUCTED),
                    cb.isNotNull(r.get("conductedOn")),
                    cb.lessThan(r.get("conductedOn"), monthStart)
            );
        }
        return (r, q, cb) -> cb.not(cb.and(
                cb.equal(r.get("lifecycleStatus"), QuizLifecycleStatus.CONDUCTED),
                cb.isNotNull(r.get("conductedOn")),
                cb.lessThan(r.get("conductedOn"), monthStart)
        ));
    }

    private Faculty loadFaculty(Long facultyId) {
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + facultyId));
    }

    private TeacherQuiz loadQuizForFaculty(Long facultyId, Long quizId) {
        TeacherQuiz qz = teacherQuizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));
        if (!qz.getTeacher().getId().equals(facultyId)) {
            throw new ResourceNotFoundException("Quiz not found with id: " + quizId);
        }
        return qz;
    }

    private TeacherQuiz loadQuizForFacultyWithQuestions(Long facultyId, Long quizId) {
        TeacherQuiz qz = teacherQuizRepository.findByIdWithQuestions(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));
        if (!qz.getTeacher().getId().equals(facultyId)) {
            throw new ResourceNotFoundException("Quiz not found with id: " + quizId);
        }
        return qz;
    }

    private void assertTeaches(Long facultyId, Long schoolId, String className, String section, Long subjectId) {
        List<ClassSubjectTeacher> list = classSubjectTeacherRepository.findBySchoolIdAndClassNameAndSectionAndSubject_IdAndActiveTrue(
                schoolId, className, section, subjectId);
        boolean ok = list.stream().anyMatch(c -> c.getFaculty().getId().equals(facultyId));
        if (!ok) {
            throw new IllegalArgumentException("Teacher is not assigned to this class and subject");
        }
    }

    private static String safeSubjectName(TeacherQuiz qz) {
        try {
            return qz.getSubject() != null && qz.getSubject().getName() != null ? qz.getSubject().getName() : "—";
        } catch (Exception e) {
            return "—";
        }
    }

    private static String classLabel(TeacherQuiz qz) {
        if (qz.getClassName() == null || qz.getSection() == null) {
            return "—";
        }
        return "Grade " + qz.getClassName() + " " + qz.getSection();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
