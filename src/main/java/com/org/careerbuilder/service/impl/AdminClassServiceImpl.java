package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.AdminClassRequests;
import com.org.careerbuilder.dto.response.AdminClassDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.AdminActivityType;
import com.org.careerbuilder.models.enums.ClassAssignmentRole;
import com.org.careerbuilder.models.enums.ClassStatus;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.AdminClassService;
import com.org.careerbuilder.service.support.AdminActivityLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminClassServiceImpl implements AdminClassService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final List<String> TABS =
            List.of("Sections", "Subjects", "Teachers", "Students", "Activity Log");

    private final SchoolRepository schoolRepository;
    private final AcademicClassRepository classRepository;
    private final ClassSectionRepository sectionRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final ClassSubjectTeacherRepository allocationRepository;
    private final StudentRepository studentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final AdminActivityLogRepository activityLogRepository;
    private final AdminActivityLogger activityLogger;

    // ─── List page ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AdminClassDtos.ClassStats getStats(Long schoolId) {
        return new AdminClassDtos.ClassStats(
                classRepository.countBySchool_IdAndDeletedFalse(schoolId),
                sectionRepository.countByAcademicClass_School_IdAndAcademicClass_DeletedFalse(schoolId),
                studentRepository.countBySchool_Id(schoolId),
                classRepository.countBySchool_IdAndDeletedFalseAndClassTeacherIsNull(schoolId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminClassDtos.AcademicYearsResponse getAcademicYears(Long schoolId) {
        List<String> years = new ArrayList<>(classRepository.findDistinctAcademicYears(schoolId));
        String current = currentAcademicYear();
        if (!years.contains(current)) {
            years.add(0, current);
        }
        return new AdminClassDtos.AcademicYearsResponse(years, current);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminClassDtos.ClassListResponse listClasses(
            Long schoolId, String q, String academicYear, String statsFilter, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 100);

        String filter = statsFilter == null ? "ALL" : statsFilter.trim().toUpperCase();
        boolean unassignedOnly = "UNASSIGNED".equals(filter);
        ClassStatus status = switch (filter) {
            case "ACTIVE" -> ClassStatus.ACTIVE;
            case "INACTIVE" -> ClassStatus.INACTIVE;
            default -> null;
        };

        Page<AcademicClass> result = classRepository.search(
                schoolId, blankToNull(q), blankToNull(academicYear), status, unassignedOnly,
                PageRequest.of(safePage, safeSize));

        List<AdminClassDtos.ClassRow> rows = result.getContent().stream()
                .map(this::toClassRow)
                .toList();

        String showing = buildShowingLabel(result.getTotalElements(), safePage, safeSize, result.getNumberOfElements());

        return new AdminClassDtos.ClassListResponse(
                getStats(schoolId),
                rows,
                safePage,
                safeSize,
                result.getTotalElements(),
                result.getTotalPages(),
                showing,
                filter,
                academicYear
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminClassDtos.CreateClassResponse createClass(AdminClassRequests.CreateClassRequest request) {
        School school = requireSchool(request.getSchoolId());
        String name = request.getName().trim();
        String year = blankToNull(request.getAcademicYear()) != null
                ? request.getAcademicYear().trim() : currentAcademicYear();

        if (classRepository.existsBySchool_IdAndNameIgnoreCaseAndAcademicYearAndDeletedFalse(
                school.getId(), name, year)) {
            throw new IllegalArgumentException("A class named '" + name + "' already exists for " + year);
        }

        AcademicClass entity = AcademicClass.builder()
                .school(school)
                .name(name)
                .academicYear(year)
                .maxCapacity(request.getMaxCapacity())
                .status(ClassStatus.INACTIVE) // becomes ACTIVE once a class teacher is assigned
                .build();
        entity = classRepository.save(entity);

        activityLogger.log(school.getId(), AdminActivityType.CLASS_CREATED,
                "Class created: " + name, year, "CLASS", entity.getId(), request.getPerformedBy());

        return new AdminClassDtos.CreateClassResponse(
                entity.getId(), name, year, "Class created successfully");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminClassDtos.ActionResponse updateClass(Long classId, AdminClassRequests.UpdateClassRequest request) {
        AcademicClass entity = requireClass(request.getSchoolId(), classId);
        String name = request.getName().trim();
        String year = blankToNull(request.getAcademicYear()) != null
                ? request.getAcademicYear().trim() : entity.getAcademicYear();

        if ((!entity.getName().equalsIgnoreCase(name) || !entity.getAcademicYear().equals(year))
                && classRepository.existsBySchool_IdAndNameIgnoreCaseAndAcademicYearAndDeletedFalse(
                request.getSchoolId(), name, year)) {
            throw new IllegalArgumentException("Another class named '" + name + "' already exists for " + year);
        }

        entity.setName(name);
        entity.setAcademicYear(year);
        if (request.getMaxCapacity() != null) {
            entity.setMaxCapacity(request.getMaxCapacity());
        }
        if (blankToNull(request.getStatus()) != null) {
            entity.setStatus(ClassStatus.fromString(request.getStatus()));
        }
        classRepository.save(entity);

        activityLogger.log(request.getSchoolId(), AdminActivityType.CLASS_UPDATED,
                "Class updated: " + name, year, "CLASS", classId, request.getPerformedBy());
        return new AdminClassDtos.ActionResponse(true, "Class updated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public AdminClassDtos.DeletePreviewResponse getDeletePreview(Long schoolId, Long classId) {
        AcademicClass entity = requireClass(schoolId, classId);
        return new AdminClassDtos.DeletePreviewResponse(
                entity.getId(),
                entity.getName(),
                "This will permanently remove " + entity.getName() + " and all associated data.");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminClassDtos.ActionResponse deleteClass(Long schoolId, Long classId, String performedBy) {
        AcademicClass entity = requireClass(schoolId, classId);
        entity.setDeleted(true);
        entity.setStatus(ClassStatus.INACTIVE);
        classRepository.save(entity);
        activityLogger.log(schoolId, AdminActivityType.CLASS_DELETED,
                "Class deleted: " + entity.getName(), entity.getAcademicYear(),
                "CLASS", classId, performedBy);
        return new AdminClassDtos.ActionResponse(true, entity.getName() + " deleted");
    }

    // ─── Detail header + form options ────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AdminClassDtos.ClassDetailResponse getClassDetail(Long schoolId, Long classId) {
        AcademicClass c = requireClass(schoolId, classId);
        long sections = sectionRepository.countByAcademicClass_Id(classId);
        long students = studentRepository.countBySchool_IdAndClassName(schoolId, c.getName());
        return new AdminClassDtos.ClassDetailResponse(
                c.getId(),
                c.getName(),
                c.getAcademicYear(),
                c.getName(),
                sections,
                students,
                teacherName(c.getClassTeacher()),
                null,
                c.getStatus().name(),
                TABS
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminClassDtos.FormOptionsResponse getFormOptions(Long schoolId, Long classId) {
        requireClass(schoolId, classId);
        List<AdminClassDtos.TeacherOption> teachers = facultyRepository
                .searchBySchool(schoolId, null, null, null, false, PageRequest.of(0, 500))
                .getContent().stream()
                .map(f -> new AdminClassDtos.TeacherOption(
                        f.getId(),
                        f.getFirstName() + " " + f.getLastName(),
                        f.getSubject().getId(),
                        f.getSubject().getName()))
                .toList();
        List<AdminClassDtos.SubjectOption> subjects = subjectRepository.findAll().stream()
                .sorted(Comparator.comparing(Subject::getName, String.CASE_INSENSITIVE_ORDER))
                .map(s -> new AdminClassDtos.SubjectOption(s.getId(), s.getName()))
                .toList();
        List<AdminClassDtos.SectionOption> sections = sectionRepository.findByClassIdWithTeacher(classId).stream()
                .map(s -> new AdminClassDtos.SectionOption(s.getId(), s.getName()))
                .toList();
        return new AdminClassDtos.FormOptionsResponse(teachers, subjects, sections);
    }

    // ─── Sections tab ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AdminClassDtos.SectionsTabResponse getSections(Long schoolId, Long classId) {
        AcademicClass c = requireClass(schoolId, classId);
        List<AdminClassDtos.SectionRow> rows = sectionRepository.findByClassIdWithTeacher(classId).stream()
                .map(s -> new AdminClassDtos.SectionRow(
                        s.getId(),
                        s.getName(),
                        teacherName(s.getSectionTeacher()),
                        null,
                        studentRepository.countBySchool_IdAndClassNameAndSection(schoolId, c.getName(), s.getName())))
                .toList();
        return new AdminClassDtos.SectionsTabResponse(rows, rows.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminClassDtos.ActionResponse addSection(Long classId, AdminClassRequests.AddSectionRequest request) {
        AcademicClass c = requireClass(request.getSchoolId(), classId);
        String name = request.getName().trim();
        if (sectionRepository.existsByAcademicClass_IdAndNameIgnoreCase(classId, name)) {
            throw new IllegalArgumentException("Section '" + name + "' already exists for this class");
        }
        ClassSection section = ClassSection.builder()
                .academicClass(c)
                .schoolId(request.getSchoolId())
                .name(name)
                .sectionTeacher(resolveFacultyOrNull(request.getSchoolId(), request.getClassTeacherId()))
                .build();
        sectionRepository.save(section);
        activityLogger.log(request.getSchoolId(), AdminActivityType.SECTION_ADDED,
                "Section added: " + name, c.getName(), "CLASS", classId, request.getPerformedBy());
        return new AdminClassDtos.ActionResponse(true, "Section created");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminClassDtos.ActionResponse updateSection(
            Long classId, Long sectionId, AdminClassRequests.UpdateSectionRequest request) {
        requireClass(request.getSchoolId(), classId);
        ClassSection section = sectionRepository.findByIdAndAcademicClass_Id(sectionId, classId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));
        if (blankToNull(request.getName()) != null) {
            section.setName(request.getName().trim());
        }
        if (request.getClassTeacherId() != null) {
            section.setSectionTeacher(resolveFacultyOrNull(request.getSchoolId(), request.getClassTeacherId()));
        }
        sectionRepository.save(section);
        activityLogger.log(request.getSchoolId(), AdminActivityType.CLASS_UPDATED,
                "Section updated: " + section.getName(), null, "CLASS", classId, request.getPerformedBy());
        return new AdminClassDtos.ActionResponse(true, "Section updated");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminClassDtos.ActionResponse deleteSection(Long schoolId, Long classId, Long sectionId, String performedBy) {
        requireClass(schoolId, classId);
        ClassSection section = sectionRepository.findByIdAndAcademicClass_Id(sectionId, classId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));
        sectionRepository.delete(section);
        activityLogger.log(schoolId, AdminActivityType.CLASS_UPDATED,
                "Section removed: " + section.getName(), null, "CLASS", classId, performedBy);
        return new AdminClassDtos.ActionResponse(true, "Section deleted");
    }

    // ─── Subjects tab ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AdminClassDtos.SubjectsTabResponse getSubjects(Long schoolId, Long classId) {
        requireClass(schoolId, classId);
        List<AdminClassDtos.SubjectRow> rows = classSubjectRepository.findByClassIdWithDetails(classId).stream()
                .map(cs -> new AdminClassDtos.SubjectRow(
                        cs.getId(),
                        cs.getSubject().getId(),
                        cs.getSubject().getName(),
                        cs.getSubjectCode(),
                        cs.getAssignedTeacher() != null ? cs.getAssignedTeacher().getId() : null,
                        teacherName(cs.getAssignedTeacher())))
                .toList();
        return new AdminClassDtos.SubjectsTabResponse(rows, rows.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminClassDtos.ActionResponse addSubject(Long classId, AdminClassRequests.AddSubjectRequest request) {
        AcademicClass c = requireClass(request.getSchoolId(), classId);
        Subject subject = resolveOrCreateSubject(request.getSubjectName());
        if (classSubjectRepository.existsByAcademicClass_IdAndSubject_Id(classId, subject.getId())) {
            throw new IllegalArgumentException(subject.getName() + " is already part of this class");
        }
        String code = blankToNull(request.getSubjectCode());
        if (code != null && classSubjectRepository.existsByAcademicClass_IdAndSubjectCodeIgnoreCase(classId, code)) {
            throw new IllegalArgumentException("Subject code '" + code + "' is already used in this class");
        }
        ClassSubject entity = ClassSubject.builder()
                .academicClass(c)
                .schoolId(request.getSchoolId())
                .subject(subject)
                .subjectCode(code)
                .assignedTeacher(resolveFacultyOrNull(request.getSchoolId(), request.getAssignedTeacherId()))
                .build();
        classSubjectRepository.save(entity);
        activityLogger.log(request.getSchoolId(), AdminActivityType.SUBJECT_ADDED,
                "Subject added: " + subject.getName(), c.getName(), "CLASS", classId, request.getPerformedBy());
        return new AdminClassDtos.ActionResponse(true, "Subject added");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminClassDtos.ActionResponse updateSubject(
            Long classId, Long classSubjectId, AdminClassRequests.UpdateSubjectRequest request) {
        requireClass(request.getSchoolId(), classId);
        ClassSubject cs = classSubjectRepository.findByIdAndAcademicClass_Id(classSubjectId, classId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found in this class"));
        if (blankToNull(request.getSubjectName()) != null) {
            cs.setSubject(resolveOrCreateSubject(request.getSubjectName()));
        }
        if (request.getSubjectCode() != null) {
            cs.setSubjectCode(blankToNull(request.getSubjectCode()));
        }
        if (request.getAssignedTeacherId() != null) {
            cs.setAssignedTeacher(resolveFacultyOrNull(request.getSchoolId(), request.getAssignedTeacherId()));
        }
        classSubjectRepository.save(cs);
        activityLogger.log(request.getSchoolId(), AdminActivityType.CLASS_UPDATED,
                "Subject updated: " + cs.getSubject().getName(), null, "CLASS", classId, request.getPerformedBy());
        return new AdminClassDtos.ActionResponse(true, "Subject updated");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminClassDtos.ActionResponse deleteSubject(Long schoolId, Long classId, Long classSubjectId, String performedBy) {
        requireClass(schoolId, classId);
        ClassSubject cs = classSubjectRepository.findByIdAndAcademicClass_Id(classSubjectId, classId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found in this class"));
        String name = cs.getSubject().getName();
        classSubjectRepository.delete(cs);
        activityLogger.log(schoolId, AdminActivityType.CLASS_UPDATED,
                "Subject removed: " + name, null, "CLASS", classId, performedBy);
        return new AdminClassDtos.ActionResponse(true, "Subject removed");
    }

    // ─── Teachers tab ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AdminClassDtos.TeachersTabResponse getTeacherAllocations(Long schoolId, Long classId) {
        AcademicClass c = requireClass(schoolId, classId);
        List<ClassSubjectTeacher> active = allocationRepository.findActiveByClass(schoolId, c.getName());

        // Group by teacher + subject + role; aggregate the sections taught.
        Map<String, List<ClassSubjectTeacher>> grouped = new LinkedHashMap<>();
        for (ClassSubjectTeacher cst : active) {
            String key = cst.getFaculty().getId() + "|" + cst.getSubject().getId() + "|"
                    + (cst.getRole() != null ? cst.getRole().name() : ClassAssignmentRole.SUBJECT_TEACHER.name());
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(cst);
        }

        List<AdminClassDtos.TeacherAllocationRow> rows = grouped.values().stream()
                .map(group -> {
                    ClassSubjectTeacher first = group.get(0);
                    ClassAssignmentRole role = first.getRole() != null
                            ? first.getRole() : ClassAssignmentRole.SUBJECT_TEACHER;
                    return new AdminClassDtos.TeacherAllocationRow(
                            first.getId(),
                            first.getFaculty().getId(),
                            first.getFaculty().getFirstName() + " " + first.getFaculty().getLastName(),
                            role.name(),
                            role.label(),
                            first.getSubject().getName(),
                            sectionsLabel(group));
                })
                .toList();
        return new AdminClassDtos.TeachersTabResponse(rows, rows.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminClassDtos.ActionResponse assignTeacher(Long classId, AdminClassRequests.AssignTeacherRequest request) {
        AcademicClass c = requireClass(request.getSchoolId(), classId);
        Faculty faculty = requireFaculty(request.getSchoolId(), request.getTeacherId());
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        String section = resolveSectionName(classId, request.getSectionId());
        ClassAssignmentRole role = ClassAssignmentRole.fromString(request.getRole());

        boolean exists = !allocationRepository
                .findBySchoolIdAndClassNameAndSectionAndSubject_IdAndActiveTrue(
                        request.getSchoolId(), c.getName(), section, subject.getId())
                .isEmpty();
        if (exists) {
            throw new IllegalArgumentException("This subject already has an active teacher for the selected section");
        }

        ClassSubjectTeacher cst = ClassSubjectTeacher.builder()
                .schoolId(request.getSchoolId())
                .className(c.getName())
                .section(section)
                .subject(subject)
                .faculty(faculty)
                .role(role)
                .assignedDate(LocalDate.now())
                .active(true)
                .build();
        allocationRepository.save(cst);

        activityLogger.log(request.getSchoolId(), AdminActivityType.TEACHER_ASSIGNED,
                "Teacher allocated: " + faculty.getFirstName() + " " + faculty.getLastName(),
                c.getName() + " • " + subject.getName(), "CLASS", classId, request.getPerformedBy());
        return new AdminClassDtos.ActionResponse(true, "Teacher assigned");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminClassDtos.ActionResponse removeAllocation(Long schoolId, Long classId, Long allocationId, String performedBy) {
        AcademicClass c = requireClass(schoolId, classId);
        ClassSubjectTeacher target = allocationRepository.findByIdAndSchoolId(allocationId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found"));
        // Deactivate the whole faculty+subject group for this class (sections were aggregated in the row).
        int removed = 0;
        for (ClassSubjectTeacher cst : allocationRepository.findActiveByClass(schoolId, c.getName())) {
            if (cst.getFaculty().getId().equals(target.getFaculty().getId())
                    && cst.getSubject().getId().equals(target.getSubject().getId())) {
                cst.setActive(false);
                allocationRepository.save(cst);
                removed++;
            }
        }
        activityLogger.log(schoolId, AdminActivityType.CLASS_UPDATED,
                "Teacher allocation removed", c.getName(), "CLASS", classId, performedBy);
        return new AdminClassDtos.ActionResponse(true, removed + " allocation(s) removed");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminClassDtos.ActionResponse assignClassTeacher(Long classId, AdminClassRequests.AssignClassTeacherRequest request) {
        AcademicClass c = requireClass(request.getSchoolId(), classId);
        Faculty faculty = requireFaculty(request.getSchoolId(), request.getTeacherId());

        c.setClassTeacher(faculty);
        c.setStatus(ClassStatus.ACTIVE);
        classRepository.save(c);

        // Optionally record a CLASS_TEACHER allocation when a subject is supplied.
        if (request.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
            String section = resolveSectionName(classId, request.getSectionId());
            boolean exists = !allocationRepository
                    .findBySchoolIdAndClassNameAndSectionAndSubject_IdAndActiveTrue(
                            request.getSchoolId(), c.getName(), section, subject.getId())
                    .isEmpty();
            if (!exists) {
                allocationRepository.save(ClassSubjectTeacher.builder()
                        .schoolId(request.getSchoolId())
                        .className(c.getName())
                        .section(section)
                        .subject(subject)
                        .faculty(faculty)
                        .role(ClassAssignmentRole.CLASS_TEACHER)
                        .assignedDate(LocalDate.now())
                        .active(true)
                        .build());
            }
        }

        activityLogger.log(request.getSchoolId(), AdminActivityType.TEACHER_ASSIGNED,
                "Class teacher assigned: " + faculty.getFirstName() + " " + faculty.getLastName(),
                c.getName(), "CLASS", classId, request.getPerformedBy());
        return new AdminClassDtos.ActionResponse(true, "Class teacher assigned");
    }

    // ─── Students tab ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AdminClassDtos.StudentsTabResponse getStudents(
            Long schoolId, Long classId, String section, int page, int size) {
        AcademicClass c = requireClass(schoolId, classId);
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(safePage, safeSize);

        Page<Student> result = blankToNull(section) != null
                ? studentRepository.findBySchool_IdAndClassNameAndSection(schoolId, c.getName(), section.trim(), pageable)
                : studentRepository.findBySchool_IdAndClassName(schoolId, c.getName(), pageable);

        Map<Long, StudentProfile> profiles = new LinkedHashMap<>();
        studentProfileRepository.findByStudent_IdIn(
                        result.getContent().stream().map(Student::getId).toList())
                .forEach(p -> profiles.put(p.getStudent().getId(), p));

        List<AdminClassDtos.EnrolledStudentRow> rows = result.getContent().stream()
                .map(s -> {
                    StudentProfile p = profiles.get(s.getId());
                    return new AdminClassDtos.EnrolledStudentRow(
                            s.getId(),
                            s.getFirstName() + " " + s.getLastName(),
                            p != null ? p.getPhotoUrl() : null,
                            p != null ? p.getAdmissionNumber() : null,
                            p != null ? p.getGender() : null,
                            s.getSection(),
                            s.getPhone(),
                            p != null && p.getAccountStatus() != null ? p.getAccountStatus().name() : "ACTIVE");
                })
                .toList();

        String showing = buildShowingLabel(result.getTotalElements(), safePage, safeSize,
                result.getNumberOfElements(), "students");
        return new AdminClassDtos.StudentsTabResponse(
                rows, safePage, safeSize, result.getTotalElements(), result.getTotalPages(), showing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminClassDtos.StudentSearchResult> searchAddableStudents(Long schoolId, Long classId, String q) {
        AcademicClass c = requireClass(schoolId, classId);
        List<Student> candidates = studentRepository.searchAddable(
                schoolId, c.getName(), blankToNull(q), PageRequest.of(0, 20));
        Map<Long, StudentProfile> profiles = new LinkedHashMap<>();
        studentProfileRepository.findByStudent_IdIn(candidates.stream().map(Student::getId).toList())
                .forEach(p -> profiles.put(p.getStudent().getId(), p));
        return candidates.stream()
                .map(s -> {
                    StudentProfile p = profiles.get(s.getId());
                    return new AdminClassDtos.StudentSearchResult(
                            s.getId(),
                            s.getFirstName() + " " + s.getLastName(),
                            p != null ? p.getAdmissionNumber() : null,
                            s.getClassName() + (s.getSection() != null ? "-" + s.getSection() : ""));
                })
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminClassDtos.ActionResponse addStudent(Long classId, AdminClassRequests.AddStudentRequest request) {
        AcademicClass c = requireClass(request.getSchoolId(), classId);
        Student student = studentRepository.findByIdAndSchool_Id(request.getStudentId(), request.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        String section = request.getSectionId() != null
                ? resolveSectionName(classId, request.getSectionId())
                : student.getSection();
        student.setClassName(c.getName());
        student.setSection(section);
        studentRepository.save(student);
        activityLogger.log(request.getSchoolId(), AdminActivityType.STUDENT_UPDATED,
                "Student added to class: " + student.getFirstName() + " " + student.getLastName(),
                c.getName() + " - " + section, "CLASS", classId, request.getPerformedBy());
        return new AdminClassDtos.ActionResponse(true, "Student added to " + c.getName() + " - " + section);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminClassDtos.ActionResponse transferStudent(
            Long classId, Long studentId, AdminClassRequests.TransferStudentRequest request) {
        requireClass(request.getSchoolId(), classId);
        Student student = studentRepository.findByIdAndSchool_Id(studentId, request.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Long targetClassId = request.getTargetClassId() != null ? request.getTargetClassId() : classId;
        AcademicClass target = requireClass(request.getSchoolId(), targetClassId);
        String section = request.getSectionId() != null
                ? resolveSectionName(targetClassId, request.getSectionId())
                : student.getSection();
        student.setClassName(target.getName());
        student.setSection(section);
        studentRepository.save(student);
        activityLogger.log(request.getSchoolId(), AdminActivityType.STUDENT_UPDATED,
                "Student transferred: " + student.getFirstName() + " " + student.getLastName(),
                target.getName() + " - " + section, "CLASS", targetClassId, request.getPerformedBy());
        return new AdminClassDtos.ActionResponse(true, "Student transferred to " + target.getName() + " - " + section);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminClassDtos.ActionResponse removeStudent(Long schoolId, Long classId, Long studentId, String performedBy) {
        AcademicClass c = requireClass(schoolId, classId);
        Student student = studentRepository.findByIdAndSchool_Id(studentId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (!c.getName().equalsIgnoreCase(student.getClassName())) {
            throw new IllegalArgumentException("Student is not enrolled in this class");
        }
        // Un-enroll without deleting the student record (string-based placement → sentinel).
        student.setClassName("UNASSIGNED");
        student.setSection("NA");
        studentRepository.save(student);
        activityLogger.log(schoolId, AdminActivityType.STUDENT_UPDATED,
                "Student removed from class: " + student.getFirstName() + " " + student.getLastName(),
                c.getName(), "CLASS", classId, performedBy);
        return new AdminClassDtos.ActionResponse(true, "Student removed from " + c.getName());
    }

    @Override
    public byte[] getBulkTemplate() {
        return AdminClassBulkHelper.template();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminClassDtos.BulkPreviewResponse previewBulkUpload(Long schoolId, Long classId, MultipartFile file) {
        AcademicClass c = requireClass(schoolId, classId);
        List<String> sectionNames = sectionRepository.findByClassIdWithTeacher(classId).stream()
                .map(ClassSection::getName).map(String::toUpperCase).toList();

        List<AdminClassDtos.BulkPreviewRow> rows = new ArrayList<>();
        int valid = 0;
        int errors = 0;
        for (AdminClassBulkHelper.ParsedRow raw : AdminClassBulkHelper.parse(file)) {
            String error = validateBulkRow(raw, schoolId, sectionNames);
            if (error == null) {
                rows.add(new AdminClassDtos.BulkPreviewRow(
                        raw.name(), raw.regNumber(), raw.section(), "PENDING", null));
                valid++;
            } else {
                rows.add(new AdminClassDtos.BulkPreviewRow(
                        raw.name(), raw.regNumber(), raw.section(), "ERROR", error));
                errors++;
            }
        }
        return new AdminClassDtos.BulkPreviewResponse(rows, valid, errors);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminClassDtos.BulkUploadResult bulkUpload(Long schoolId, Long classId, String performedBy, MultipartFile file) {
        AcademicClass c = requireClass(schoolId, classId);
        List<String> sectionNames = sectionRepository.findByClassIdWithTeacher(classId).stream()
                .map(ClassSection::getName).map(String::toUpperCase).toList();

        int added = 0;
        int skipped = 0;
        for (AdminClassBulkHelper.ParsedRow raw : AdminClassBulkHelper.parse(file)) {
            if (validateBulkRow(raw, schoolId, sectionNames) != null) {
                skipped++;
                continue;
            }
            StudentProfile profile = studentProfileRepository
                    .findByAdmissionNumberIgnoreCase(raw.regNumber().trim()).orElse(null);
            if (profile == null) {
                skipped++;
                continue;
            }
            Student student = profile.getStudent();
            student.setClassName(c.getName());
            student.setSection(raw.section().trim());
            studentRepository.save(student);
            added++;
        }
        activityLogger.log(schoolId, AdminActivityType.STUDENT_UPDATED,
                "Bulk enrolled students", added + " added to " + c.getName(),
                "CLASS", classId, performedBy);
        return new AdminClassDtos.BulkUploadResult(added, skipped,
                added + " student(s) enrolled, " + skipped + " skipped");
    }

    // ─── Activity log tab ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AdminClassDtos.ActivityLogTabResponse getActivityLog(Long schoolId, Long classId, int page, int size) {
        requireClass(schoolId, classId);
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Page<AdminActivityLog> result = activityLogRepository.searchStudentActivityLog(
                schoolId, "CLASS", classId, null, null, PageRequest.of(safePage, safeSize));
        List<AdminClassDtos.ActivityRow> rows = result.getContent().stream()
                .map(this::toActivityRow)
                .toList();
        return new AdminClassDtos.ActivityLogTabResponse(
                rows, safePage, safeSize, result.getTotalElements(), result.hasNext());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────────

    private AdminClassDtos.ClassRow toClassRow(AcademicClass c) {
        List<String> sections = sectionRepository.findByClassIdWithTeacher(c.getId()).stream()
                .map(ClassSection::getName)
                .toList();
        return new AdminClassDtos.ClassRow(
                c.getId(),
                c.getName(),
                sections,
                teacherName(c.getClassTeacher()),
                null,
                studentRepository.countBySchool_IdAndClassName(c.getSchool().getId(), c.getName()),
                c.getMaxCapacity(),
                c.getStatus().name(),
                c.getClassTeacher() == null
        );
    }

    private AdminClassDtos.ActivityRow toActivityRow(AdminActivityLog log) {
        return new AdminClassDtos.ActivityRow(
                log.getId(),
                log.getTitle(),
                mapModule(log.getActivityType()),
                log.getCreatedAt() != null ? log.getCreatedAt().format(DATE_FMT) : "",
                log.getPerformedBy() != null ? log.getPerformedBy() : "System");
    }

    private static String mapModule(AdminActivityType type) {
        return switch (type) {
            case SECTION_ADDED -> "Sections";
            case SUBJECT_ADDED -> "Subjects";
            case TEACHER_ASSIGNED -> "Teachers";
            default -> "Class";
        };
    }

    private String resolveSectionName(Long classId, Long sectionId) {
        if (sectionId == null) {
            return "ALL";
        }
        return sectionRepository.findByIdAndAcademicClass_Id(sectionId, classId)
                .map(ClassSection::getName)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found for this class"));
    }

    private static String sectionsLabel(List<ClassSubjectTeacher> group) {
        List<String> sections = group.stream()
                .map(ClassSubjectTeacher::getSection)
                .distinct()
                .sorted()
                .toList();
        if (sections.contains("ALL")) {
            return "ALL";
        }
        return String.join(", ", sections);
    }

    private Subject resolveOrCreateSubject(String rawName) {
        String name = rawName.trim();
        return subjectRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> subjectRepository.save(Subject.builder().name(name).build()));
    }

    private Faculty resolveFacultyOrNull(Long schoolId, Long facultyId) {
        if (facultyId == null) {
            return null;
        }
        return requireFaculty(schoolId, facultyId);
    }

    private Faculty requireFaculty(Long schoolId, Long facultyId) {
        return facultyRepository.findByIdAndSchool_Id(facultyId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
    }

    private AcademicClass requireClass(Long schoolId, Long classId) {
        return classRepository.findActiveById(classId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
    }

    private School requireSchool(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));
    }

    private static String teacherName(Faculty f) {
        return f != null ? f.getFirstName() + " " + f.getLastName() : null;
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    /** Indian academic year convention: April–March (e.g. 2025-2026). */
    private static String currentAcademicYear() {
        LocalDate now = LocalDate.now();
        int startYear = now.getMonthValue() >= 4 ? now.getYear() : now.getYear() - 1;
        return startYear + "-" + (startYear + 1);
    }

    private static String buildShowingLabel(long total, int page, int size, int onPage) {
        return buildShowingLabel(total, page, size, onPage, "classes");
    }

    private static String buildShowingLabel(long total, int page, int size, int onPage, String noun) {
        if (total == 0) {
            return "Showing 0 of 0 " + noun;
        }
        long from = (long) page * size + 1;
        long to = from + onPage - 1;
        return "Showing " + from + "-" + to + " of " + total + " " + noun;
    }

    private String validateBulkRow(AdminClassBulkHelper.ParsedRow raw, Long schoolId, List<String> sectionNames) {
        if (raw.regNumber() == null || raw.regNumber().isBlank()) {
            return "Registration number is required";
        }
        if (raw.section() == null || raw.section().isBlank()) {
            return "Section is required";
        }
        if (!sectionNames.contains(raw.section().trim().toUpperCase())) {
            return "Section '" + raw.section() + "' does not exist in this class";
        }
        StudentProfile profile = studentProfileRepository
                .findByAdmissionNumberIgnoreCase(raw.regNumber().trim()).orElse(null);
        if (profile == null) {
            return "No student found with this registration number";
        }
        Student student = profile.getStudent();
        if (student == null || student.getSchool() == null || !student.getSchool().getId().equals(schoolId)) {
            return "Student belongs to another school";
        }
        return null;
    }
}
