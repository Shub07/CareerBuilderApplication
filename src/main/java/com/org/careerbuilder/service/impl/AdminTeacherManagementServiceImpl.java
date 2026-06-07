package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.AdminQuickActionRequests;
import com.org.careerbuilder.dto.request.AdminTeacherRequests;
import com.org.careerbuilder.dto.response.AdminOperationResponses;
import com.org.careerbuilder.dto.response.AdminTeacherDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.AdminActivityType;
import com.org.careerbuilder.models.enums.ClassAssignmentRole;
import com.org.careerbuilder.models.enums.FacultyAccountStatus;
import com.org.careerbuilder.models.enums.FacultyEmploymentType;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.AdminTeacherManagementService;
import com.org.careerbuilder.service.support.AdminActivityLogger;
import com.org.careerbuilder.service.support.AdminFileStorageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTeacherManagementServiceImpl implements AdminTeacherManagementService {

    private static final long MAX_DOC_BYTES = 5L * 1024 * 1024;
    private static final Set<String> DOC_EXT = Set.of("pdf", "doc", "docx");
    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png");

    private final SchoolRepository schoolRepository;
    private final FacultyRepository facultyRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final SubjectRepository subjectRepository;
    private final ClassSubjectTeacherRepository assignmentRepository;
    private final StudentRepository studentRepository;
    private final AdminFileStorageHelper fileStorageHelper;
    private final AdminActivityLogger activityLogger;

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherDtos.TeacherStats getStats(Long schoolId) {
        assertSchool(schoolId);
        long total = facultyProfileRepository.countBySchoolAndStatus(schoolId, FacultyAccountStatus.ACTIVE)
                + facultyProfileRepository.countBySchoolAndStatus(schoolId, FacultyAccountStatus.ON_LEAVE)
                + facultyProfileRepository.countBySchoolAndStatus(schoolId, FacultyAccountStatus.INACTIVE);
        long active = facultyProfileRepository.countBySchoolAndStatus(schoolId, FacultyAccountStatus.ACTIVE);
        long onLeave = facultyProfileRepository.countBySchoolAndStatus(schoolId, FacultyAccountStatus.ON_LEAVE);
        long unassigned = facultyProfileRepository.countUnassignedBySchoolId(schoolId);
        return new AdminTeacherDtos.TeacherStats(total, active, onLeave, unassigned);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherDtos.TeacherFilterOptions getFilterOptions(Long schoolId) {
        assertSchool(schoolId);
        List<AdminTeacherDtos.SubjectOption> subjects = subjectRepository.findAll().stream()
                .sorted(Comparator.comparing(Subject::getName, String.CASE_INSENSITIVE_ORDER))
                .map(s -> new AdminTeacherDtos.SubjectOption(s.getId(), s.getName()))
                .toList();

        Map<String, List<String>> classSections = new TreeMap<>();
        for (Object[] row : studentRepository.findDistinctClassSections(schoolId)) {
            classSections.computeIfAbsent((String) row[0], k -> new ArrayList<>()).add((String) row[1]);
        }
        List<AdminTeacherDtos.ClassSectionOption> classes = classSections.entrySet().stream()
                .map(e -> new AdminTeacherDtos.ClassSectionOption(e.getKey(), e.getValue()))
                .toList();

        return new AdminTeacherDtos.TeacherFilterOptions(
                subjects,
                Arrays.stream(FacultyAccountStatus.values()).map(Enum::name).toList(),
                List.of("ALL", "ACTIVE", "ON_LEAVE", "INACTIVE", "UNASSIGNED"),
                classes
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherDtos.AcademicYearsResponse getAcademicYears(Long schoolId) {
        assertSchool(schoolId);
        String current = currentAcademicYear();
        return new AdminTeacherDtos.AcademicYearsResponse(List.of(current), current);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherDtos.TeacherSectionResponse listTeachers(AdminTeacherRequests.TeacherListQuery query) {
        return listTeachers(
                query.getSchoolId(),
                query.getAcademicYear(),
                query.getQ(),
                query.getSubjectId(),
                query.getStatus(),
                query.getStatsFilter(),
                query.getPage(),
                query.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherDtos.TeacherSectionResponse listTeachers(
            Long schoolId, String academicYear, String q, Long subjectId,
            String status, String statsFilter, int page, int size) {
        assertSchool(schoolId);
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 100);
        FilterResolution filters = resolveFilters(status, statsFilter);

        Page<Faculty> pageResult = facultyRepository.searchBySchool(
                schoolId,
                blankToNull(q),
                subjectId,
                filters.status(),
                filters.unassignedOnly(),
                PageRequest.of(safePage, safeSize));

        List<Long> facultyIds = pageResult.getContent().stream().map(Faculty::getId).toList();
        Map<Long, List<ClassSubjectTeacher>> assignmentsByFaculty = assignmentRepository
                .findActiveByFacultyIds(facultyIds).stream()
                .collect(Collectors.groupingBy(cst -> cst.getFaculty().getId()));

        Map<Long, FacultyProfile> profiles = facultyIds.isEmpty()
                ? Map.of()
                : facultyIds.stream()
                .map(id -> facultyProfileRepository.findByFaculty_Id(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(p -> p.getFaculty().getId(), p -> p));

        List<AdminTeacherDtos.TeacherRow> rows = pageResult.getContent().stream()
                .map(f -> toRow(f, profiles.get(f.getId()), assignmentsByFaculty.getOrDefault(f.getId(), List.of())))
                .toList();

        AdminTeacherDtos.TeacherStats stats = getStats(schoolId);
        String showingLabel = buildShowingLabel(safePage, safeSize, pageResult.getTotalElements());
        return new AdminTeacherDtos.TeacherSectionResponse(
                stats,
                rows,
                safePage,
                safeSize,
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                filters.statsFilterLabel(),
                filters.statusDropdownLabel(),
                subjectId,
                showingLabel
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationResponses.BulkActionResponse bulkChangeStatus(AdminTeacherRequests.BulkStatusRequest request) {
        assertSchool(request.getSchoolId());
        FacultyAccountStatus status = parseStatusRequired(request.getStatus());
        int count = 0;
        for (Long facultyId : request.getFacultyIds()) {
            Faculty faculty = requireFaculty(request.getSchoolId(), facultyId);
            FacultyProfile profile = requireProfile(faculty);
            profile.setAccountStatus(status);
            facultyProfileRepository.save(profile);
            count++;
        }
        activityLogger.log(request.getSchoolId(), AdminActivityType.TEACHER_UPDATED,
                "Bulk teacher status update",
                status.name() + " — " + count + " teacher(s)",
                "FACULTY", null, request.getPerformedBy());
        return new AdminOperationResponses.BulkActionResponse(count, count + " teacher(s) updated");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationResponses.BulkActionResponse bulkDelete(AdminTeacherRequests.BulkDeleteRequest request) {
        assertSchool(request.getSchoolId());
        int count = 0;
        for (Long facultyId : request.getFacultyIds()) {
            deleteTeacher(request.getSchoolId(), facultyId, request.getPerformedBy());
            count++;
        }
        return new AdminOperationResponses.BulkActionResponse(count, count + " teacher(s) removed");
    }

    @Override
    public AdminTeacherDtos.ExportFieldCatalogResponse getExportFieldCatalog() {
        Map<String, List<String>> catalog = Map.of(
                "basic", List.of("teacherName", "employeeId", "gender", "dob", "phone", "email", "address", "profilePhoto"),
                "professional", List.of("subjectSpecialization", "qualification", "experience", "certifications", "skills"),
                "employment", List.of("joiningDate", "employmentType", "status", "assignedClasses", "assignedSubjects", "department", "salary"),
                "documents", List.of("idProof", "certificates", "resume", "contract", "otherDocuments")
        );
        return new AdminTeacherDtos.ExportFieldCatalogResponse(catalog, AdminTeacherExportWriter.fieldLabels());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherDtos.ExportPreviewResponse previewExport(AdminTeacherRequests.ExportRequest request) {
        assertSchool(request.getSchoolId());
        List<TeacherExportBundle> bundles = resolveTeachersForExport(request);
        String scope = request.getFacultyIds() != null && !request.getFacultyIds().isEmpty()
                ? "SELECTED" : "FILTERED";
        return new AdminTeacherDtos.ExportPreviewResponse(
                bundles.size(),
                scope,
                List.of("teacherName", "employeeId", "subjectSpecialization", "assignedClasses", "status", "email")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherManagementService.AdminTeacherExportResult exportTeachers(
            AdminTeacherRequests.ExportRequest request) {
        assertSchool(request.getSchoolId());
        List<TeacherExportBundle> bundles = resolveTeachersForExport(request);
        if (bundles.isEmpty()) {
            throw new IllegalArgumentException("No teachers match the export criteria");
        }

        List<String> selectedFields = resolveExportFields(request.getFields());
        List<String> headers = selectedFields.stream()
                .map(f -> AdminTeacherExportWriter.fieldLabels().getOrDefault(f, f))
                .toList();

        List<Map<String, String>> rows = bundles.stream()
                .map(b -> toExportMap(b, selectedFields, headers))
                .toList();

        String format = request.getFormat() == null ? "excel" : request.getFormat().trim().toLowerCase();
        AdminTeacherManagementService.AdminTeacherExportResult result = switch (format) {
            case "csv" -> new AdminTeacherManagementService.AdminTeacherExportResult(
                    AdminTeacherExportWriter.writeCsv(headers, rows),
                    "text/csv",
                    "teachers-export.csv"
            );
            case "pdf" -> new AdminTeacherManagementService.AdminTeacherExportResult(
                    AdminTeacherExportWriter.writePdf(headers, rows),
                    "application/pdf",
                    "teachers-export.pdf"
            );
            default -> new AdminTeacherManagementService.AdminTeacherExportResult(
                    AdminTeacherExportWriter.writeExcel(headers, rows),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "teachers-export.xlsx"
            );
        };

        activityLogger.log(request.getSchoolId(), AdminActivityType.OTHER,
                "Teachers exported",
                bundles.size() + " teacher(s) — " + format.toUpperCase(),
                "FACULTY", null, request.getPerformedBy());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherDtos.TeacherDetailResponse getTeacherDetail(Long schoolId, Long facultyId) {
        Faculty faculty = requireFaculty(schoolId, facultyId);
        FacultyProfile profile = requireProfile(faculty);
        List<ClassSubjectTeacher> assignments = assignmentRepository.findByFaculty_IdAndActiveTrueWithSubject(facultyId);
        return toDetail(faculty, profile, assignments);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherDtos.TeacherProfileSummaryResponse getProfileSummary(Long schoolId, Long facultyId) {
        Faculty faculty = requireFaculty(schoolId, facultyId);
        FacultyProfile profile = requireProfile(faculty);
        List<ClassSubjectTeacher> assignments = assignmentRepository.findByFaculty_IdAndActiveTrueWithSubject(facultyId);
        List<String> classes = assignments.stream()
                .map(a -> a.getClassName() + "-" + a.getSection())
                .distinct()
                .sorted()
                .toList();
        return new AdminTeacherDtos.TeacherProfileSummaryResponse(
                faculty.getId(),
                faculty.getFacultyId(),
                faculty.getFirstName() + " " + faculty.getLastName(),
                faculty.getEmail(),
                faculty.getPhone(),
                profile.getPhotoUrl(),
                faculty.getSubject().getName(),
                profile.getAccountStatus().name(),
                classes,
                profile.getJoiningDate()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherDtos.EditFormResponse getEditForm(Long schoolId, Long facultyId) {
        AdminTeacherDtos.TeacherDetailResponse detail = getTeacherDetail(schoolId, facultyId);
        return new AdminTeacherDtos.EditFormResponse(
                detail,
                List.of("Male", "Female", "Other"),
                Arrays.stream(FacultyEmploymentType.values()).map(Enum::name).toList(),
                List.of("Science", "Arts", "Commerce", "Languages", "Physical Education", "Administration"),
                subjectRepository.findAll().stream()
                        .sorted(Comparator.comparing(Subject::getName, String.CASE_INSENSITIVE_ORDER))
                        .map(s -> new AdminTeacherDtos.SubjectOption(s.getId(), s.getName()))
                        .toList(),
                buildClassSectionOptions(schoolId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherDtos.DeletePreviewResponse getDeletePreview(Long schoolId, Long facultyId) {
        Faculty faculty = requireFaculty(schoolId, facultyId);
        String fullName = faculty.getFirstName() + " " + faculty.getLastName();
        return new AdminTeacherDtos.DeletePreviewResponse(
                facultyId,
                fullName,
                faculty.getFacultyId(),
                "This action cannot be undone. All associated data for " + fullName
                        + " will be permanently removed."
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherDtos.AllocateClassesFormResponse getAllocateClassesForm(Long schoolId, Long facultyId) {
        Faculty faculty = requireFaculty(schoolId, facultyId);
        List<ClassSubjectTeacher> current = assignmentRepository.findByFaculty_IdAndActiveTrueWithSubject(facultyId);
        Set<String> selectedKeys = current.stream()
                .map(a -> a.getClassName() + "|" + a.getSection())
                .collect(Collectors.toSet());

        List<AdminTeacherDtos.ClassSectionCheckbox> checkboxes = new ArrayList<>();
        for (AdminTeacherDtos.ClassSectionOption opt : buildClassSectionOptions(schoolId)) {
            for (String section : opt.sections()) {
                String key = opt.className() + "|" + section;
                checkboxes.add(new AdminTeacherDtos.ClassSectionCheckbox(
                        opt.className(),
                        section,
                        "Grade " + opt.className() + "-" + section,
                        selectedKeys.contains(key)
                ));
            }
        }

        List<AdminTeacherDtos.SubjectOption> subjects = subjectRepository.findAll().stream()
                .sorted(Comparator.comparing(Subject::getName, String.CASE_INSENSITIVE_ORDER))
                .map(s -> new AdminTeacherDtos.SubjectOption(s.getId(), s.getName()))
                .toList();

        return new AdminTeacherDtos.AllocateClassesFormResponse(
                faculty.getId(),
                faculty.getFirstName() + " " + faculty.getLastName(),
                faculty.getSubject().getId(),
                faculty.getSubject().getName(),
                subjects,
                checkboxes,
                current.stream().map(AdminTeacherManagementServiceImpl::toAssignmentRow).toList()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminTeacherDtos.AllocateClassesResponse allocateClasses(
            Long facultyId, AdminTeacherRequests.AllocateClassesRequest request) {
        Faculty faculty = requireFaculty(request.getSchoolId(), facultyId);
        int deactivated = 0;
        if (request.isReplaceExisting()) {
            List<ClassSubjectTeacher> existing = assignmentRepository.findByFaculty_IdAndActiveTrue(facultyId);
            for (ClassSubjectTeacher cst : existing) {
                cst.setActive(false);
                assignmentRepository.save(cst);
                deactivated++;
            }
        }
        ClassAssignmentRole requestRole = ClassAssignmentRole.fromString(request.getRole());
        int created = 0;
        int skipped = 0;
        for (AdminTeacherRequests.ClassAssignmentInput section : request.getClassSections()) {
            ClassAssignmentRole rowRole = section.getRole() != null
                    ? ClassAssignmentRole.fromString(section.getRole())
                    : requestRole;
            try {
                saveAssignment(request.getSchoolId(), faculty,
                        section.getClassName(), section.getSection(), request.getSubjectId(), rowRole);
                created++;
            } catch (IllegalArgumentException e) {
                skipped++;
            }
        }
        List<ClassSubjectTeacher> updated = assignmentRepository.findByFaculty_IdAndActiveTrueWithSubject(facultyId);
        activityLogger.log(request.getSchoolId(), AdminActivityType.TEACHER_ASSIGNED,
                "Classes allocated",
                created + " assignment(s) for " + faculty.getFacultyId(),
                "FACULTY", facultyId, request.getPerformedBy());
        return new AdminTeacherDtos.AllocateClassesResponse(
                created,
                skipped,
                deactivated,
                created + " class assignment(s) saved",
                updated.stream().map(AdminTeacherManagementServiceImpl::toAssignmentRow).toList()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminTeacherDtos.TeacherCreatedResponse createTeacher(
            AdminTeacherRequests.CreateTeacherRequest request,
            MultipartFile photo,
            MultipartFile resume,
            MultipartFile idProof) {
        School school = assertSchool(request.getSchoolId());
        validateEmailUnique(request.getEmail(), null);
        Subject subject = resolveSubject(request.getSubjectId(), request.getSpecialization());
        String employeeId = resolveEmployeeId(request.getEmployeeId(), school.getSchoolCode());

        Faculty faculty = Faculty.builder()
                .facultyId(employeeId)
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .gender(request.getGender().trim())
                .age(computeAge(request.getDateOfBirth()))
                .subject(subject)
                .qualification(request.getQualification().trim())
                .experience(request.getExperienceYears())
                .phone(request.getPhone().trim())
                .email(request.getEmail().trim().toLowerCase())
                .address(request.getAddress().trim())
                .school(school)
                .build();
        faculty = facultyRepository.save(faculty);

        FacultyProfile profile = FacultyProfile.builder()
                .faculty(faculty)
                .accountStatus(FacultyAccountStatus.ACTIVE)
                .dateOfBirth(request.getDateOfBirth())
                .joiningDate(request.getJoiningDate() != null ? request.getJoiningDate() : LocalDate.now())
                .employmentType(parseEmploymentType(request.getEmploymentType()))
                .department(request.getDepartment())
                .skills(request.getSkills())
                .certifications(request.getCertifications())
                .build();
        applyFiles(profile, request.getSchoolId(), photo, resume, idProof, null, null);
        facultyProfileRepository.save(profile);

        if (request.getInitialAssignments() != null) {
            for (AdminTeacherRequests.ClassAssignmentInput a : request.getInitialAssignments()) {
                saveAssignment(request.getSchoolId(), faculty, a.getClassName(), a.getSection(), a.getSubjectId());
            }
        }

        String fullName = faculty.getFirstName() + " " + faculty.getLastName();
        activityLogger.log(school.getId(), AdminActivityType.TEACHER_ADDED,
                "Teacher created: " + fullName,
                subject.getName() + " — " + employeeId,
                "FACULTY", faculty.getId(), request.getPerformedBy());

        return new AdminTeacherDtos.TeacherCreatedResponse(
                faculty.getId(), employeeId, fullName, faculty.getEmail(), "Teacher created successfully");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminTeacherDtos.TeacherDetailResponse updateTeacher(
            Long facultyId,
            AdminTeacherRequests.UpdateTeacherRequest request,
            MultipartFile photo,
            MultipartFile resume,
            MultipartFile idProof,
            MultipartFile certificates,
            MultipartFile experienceLetters) {
        Faculty faculty = requireFaculty(request.getSchoolId(), facultyId);
        FacultyProfile profile = requireProfile(faculty);
        validateEmailUnique(request.getEmail(), facultyId);

        if (request.getEmployeeId() != null && !request.getEmployeeId().isBlank()
                && !request.getEmployeeId().equalsIgnoreCase(faculty.getFacultyId())) {
            if (facultyRepository.existsByFacultyIdIgnoreCase(request.getEmployeeId())) {
                throw new IllegalArgumentException("Employee ID already exists");
            }
            faculty.setFacultyId(request.getEmployeeId().trim());
        }

        Subject subject = resolveSubject(request.getSubjectId(), request.getSpecialization());
        faculty.setFirstName(request.getFirstName().trim());
        faculty.setLastName(request.getLastName().trim());
        faculty.setGender(request.getGender().trim());
        faculty.setAge(computeAge(request.getDateOfBirth()));
        faculty.setSubject(subject);
        faculty.setQualification(request.getQualification().trim());
        faculty.setExperience(request.getExperienceYears());
        faculty.setPhone(request.getPhone().trim());
        faculty.setEmail(request.getEmail().trim().toLowerCase());
        faculty.setAddress(request.getAddress().trim());
        facultyRepository.save(faculty);

        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setJoiningDate(request.getJoiningDate());
        profile.setEmploymentType(parseEmploymentType(request.getEmploymentType()));
        profile.setDepartment(request.getDepartment());
        profile.setSkills(request.getSkills());
        profile.setCertifications(request.getCertifications());
        applyFiles(profile, request.getSchoolId(), photo, resume, idProof, certificates, experienceLetters);
        facultyProfileRepository.save(profile);

        if (request.getClassAssignments() != null) {
            syncClassAssignments(faculty, request.getSchoolId(), request.getClassAssignments(),
                    Boolean.TRUE.equals(request.getReplaceClassAssignments()));
        }

        activityLogger.log(request.getSchoolId(), AdminActivityType.TEACHER_UPDATED,
                "Teacher updated: " + faculty.getFirstName() + " " + faculty.getLastName(),
                faculty.getFacultyId(),
                "FACULTY", facultyId, request.getPerformedBy());

        return getTeacherDetail(request.getSchoolId(), facultyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminTeacherDtos.ActionResponse updateStatus(Long facultyId, AdminTeacherRequests.UpdateStatusRequest request) {
        Faculty faculty = requireFaculty(request.getSchoolId(), facultyId);
        FacultyProfile profile = requireProfile(faculty);
        FacultyAccountStatus status = parseStatusRequired(request.getStatus());
        profile.setAccountStatus(status);
        facultyProfileRepository.save(profile);
        activityLogger.log(request.getSchoolId(), AdminActivityType.TEACHER_UPDATED,
                "Teacher status changed to " + status.name(),
                faculty.getFacultyId(),
                "FACULTY", facultyId, request.getPerformedBy());
        return new AdminTeacherDtos.ActionResponse(true, "Status updated");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminTeacherDtos.DeleteTeacherResponse deleteTeacher(Long schoolId, Long facultyId, String performedBy) {
        Faculty faculty = requireFaculty(schoolId, facultyId);
        FacultyProfile profile = requireProfile(faculty);
        String fullName = faculty.getFirstName() + " " + faculty.getLastName();
        profile.setDeleted(true);
        profile.setAccountStatus(FacultyAccountStatus.INACTIVE);
        facultyProfileRepository.save(profile);
        deactivateAllAssignments(facultyId);
        activityLogger.log(schoolId, AdminActivityType.TEACHER_DEACTIVATED,
                "Teacher removed: " + fullName,
                faculty.getFacultyId(),
                "FACULTY", facultyId, performedBy);
        return new AdminTeacherDtos.DeleteTeacherResponse(
                true,
                "Teacher record removed successfully",
                fullName,
                facultyId
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminTeacherDtos.AssignmentCreatedResponse addAssignment(
            Long facultyId, AdminTeacherRequests.AddAssignmentRequest request) {
        Faculty faculty = requireFaculty(request.getSchoolId(), facultyId);
        ClassSubjectTeacher cst = saveAssignment(
                request.getSchoolId(), faculty, request.getClassName(), request.getSection(), request.getSubjectId());
        activityLogger.log(request.getSchoolId(), AdminActivityType.TEACHER_ASSIGNED,
                "Class assigned",
                request.getClassName() + "-" + request.getSection(),
                "FACULTY", facultyId, request.getPerformedBy());
        return new AdminTeacherDtos.AssignmentCreatedResponse(cst.getId(), "Assignment created");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminTeacherDtos.ActionResponse removeAssignment(
            Long schoolId, Long facultyId, Long assignmentId, String performedBy) {
        requireFaculty(schoolId, facultyId);
        ClassSubjectTeacher cst = assignmentRepository.findByIdAndFaculty_Id(assignmentId, facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        cst.setActive(false);
        assignmentRepository.save(cst);
        return new AdminTeacherDtos.ActionResponse(true, "Assignment removed");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationResponses.TeacherCreatedResponse quickCreate(AdminQuickActionRequests.AddTeacherRequest request) {
        School school = assertSchool(request.getSchoolId());
        if (facultyRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Teacher email already exists");
        }
        Subject subject = subjectRepository.findByNameIgnoreCase(request.getSubjectName().trim())
                .orElseGet(() -> subjectRepository.save(Subject.builder().name(request.getSubjectName().trim()).build()));

        String[] names = splitName(request.getTeacherName());
        String facultyCode = generateFacultyCode(school.getSchoolCode());

        Faculty faculty = Faculty.builder()
                .facultyId(facultyCode)
                .firstName(names[0])
                .lastName(names[1])
                .gender("Not Specified")
                .age(30)
                .subject(subject)
                .qualification("Not Provided")
                .experience(0)
                .phone(request.getPhone())
                .email(request.getEmail().trim().toLowerCase())
                .address(school.getVillageTownCity() != null ? school.getVillageTownCity() : "School Campus")
                .school(school)
                .build();
        faculty = facultyRepository.save(faculty);

        FacultyProfile profile = FacultyProfile.builder()
                .faculty(faculty)
                .accountStatus(FacultyAccountStatus.ACTIVE)
                .joiningDate(LocalDate.now())
                .photoUrl(request.getPhotoUrl())
                .build();
        facultyProfileRepository.save(profile);

        activityLogger.log(school.getId(), AdminActivityType.TEACHER_ADDED,
                "Teacher added: " + request.getTeacherName(),
                subject.getName() + " — " + facultyCode,
                "FACULTY", faculty.getId(), request.getPerformedBy());

        return new AdminOperationResponses.TeacherCreatedResponse(
                faculty.getId(), faculty.getFacultyId(), request.getTeacherName(), faculty.getEmail());
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private ClassSubjectTeacher saveAssignment(
            Long schoolId, Faculty faculty, String className, String section, Long subjectId) {
        return saveAssignment(schoolId, faculty, className, section, subjectId, ClassAssignmentRole.SUBJECT_TEACHER);
    }

    private ClassSubjectTeacher saveAssignment(
            Long schoolId, Faculty faculty, String className, String section, Long subjectId,
            ClassAssignmentRole role) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        List<ClassSubjectTeacher> existing = assignmentRepository
                .findBySchoolIdAndClassNameAndSectionAndSubject_IdAndActiveTrue(
                        schoolId, className, section, subjectId);
        if (!existing.isEmpty()) {
            throw new IllegalArgumentException("This class-section-subject already has an active teacher");
        }
        ClassSubjectTeacher cst = ClassSubjectTeacher.builder()
                .schoolId(schoolId)
                .className(className)
                .section(section)
                .subject(subject)
                .faculty(faculty)
                .active(true)
                .role(role != null ? role : ClassAssignmentRole.SUBJECT_TEACHER)
                .assignedDate(LocalDate.now())
                .build();
        return assignmentRepository.save(cst);
    }

    /** Shared mapper so every allocation row carries role + assigned date. */
    static AdminTeacherDtos.ClassAssignmentRow toAssignmentRow(ClassSubjectTeacher a) {
        ClassAssignmentRole role = a.getRole() != null ? a.getRole() : ClassAssignmentRole.SUBJECT_TEACHER;
        return new AdminTeacherDtos.ClassAssignmentRow(
                a.getId(),
                a.getClassName(),
                a.getSection(),
                a.getSubject().getId(),
                a.getSubject().getName(),
                role.name(),
                role.label(),
                a.getAssignedDate(),
                a.isActive());
    }

    private AdminTeacherDtos.TeacherRow toRow(
            Faculty f, FacultyProfile profile, List<ClassSubjectTeacher> assignments) {
        List<String> classLabels = assignments.stream()
                .map(a -> a.getClassName() + "-" + a.getSection())
                .distinct()
                .sorted()
                .toList();
        String status = profile != null ? profile.getAccountStatus().name() : FacultyAccountStatus.ACTIVE.name();
        return new AdminTeacherDtos.TeacherRow(
                f.getId(),
                f.getFacultyId(),
                f.getFirstName() + " " + f.getLastName(),
                f.getEmail(),
                profile != null ? profile.getPhotoUrl() : null,
                f.getSubject().getName().toUpperCase(),
                f.getSubject().getId(),
                classLabels,
                status,
                classLabels.isEmpty()
        );
    }

    private AdminTeacherDtos.TeacherDetailResponse toDetail(
            Faculty f, FacultyProfile profile, List<ClassSubjectTeacher> assignments) {
        List<AdminTeacherDtos.ClassAssignmentRow> assignmentRows = assignments.stream()
                .map(AdminTeacherManagementServiceImpl::toAssignmentRow)
                .toList();
        return new AdminTeacherDtos.TeacherDetailResponse(
                f.getId(),
                f.getFacultyId(),
                f.getFirstName(),
                f.getLastName(),
                f.getFirstName() + " " + f.getLastName(),
                f.getGender(),
                profile.getDateOfBirth(),
                f.getAge(),
                f.getPhone(),
                f.getEmail(),
                f.getAddress(),
                profile.getPhotoUrl(),
                f.getQualification(),
                f.getSubject().getName(),
                f.getSubject().getId(),
                f.getExperience(),
                profile.getSkills(),
                profile.getCertifications(),
                profile.getJoiningDate(),
                profile.getEmploymentType() != null ? profile.getEmploymentType().name() : null,
                profile.getDepartment(),
                profile.getAccountStatus().name(),
                profile.getResumePath(),
                profile.getIdProofPath(),
                profile.getCertificatesPath(),
                profile.getExperienceLettersPath(),
                assignmentRows
        );
    }

    private List<AdminTeacherDtos.ClassSectionOption> buildClassSectionOptions(Long schoolId) {
        Map<String, List<String>> classSections = new TreeMap<>();
        for (Object[] row : studentRepository.findDistinctClassSections(schoolId)) {
            classSections.computeIfAbsent((String) row[0], k -> new ArrayList<>()).add((String) row[1]);
        }
        return classSections.entrySet().stream()
                .map(e -> new AdminTeacherDtos.ClassSectionOption(e.getKey(), e.getValue()))
                .toList();
    }

    private void syncClassAssignments(
            Faculty faculty, Long schoolId,
            List<AdminTeacherRequests.ClassAssignmentInput> assignments,
            boolean replace) {
        if (replace) {
            deactivateAllAssignments(faculty.getId());
        }
        for (AdminTeacherRequests.ClassAssignmentInput a : assignments) {
            Long subjectId = a.getSubjectId() != null ? a.getSubjectId() : faculty.getSubject().getId();
            try {
                saveAssignment(schoolId, faculty, a.getClassName(), a.getSection(), subjectId);
            } catch (IllegalArgumentException ignored) {
                // skip duplicate class-section-subject
            }
        }
    }

    private void deactivateAllAssignments(Long facultyId) {
        for (ClassSubjectTeacher cst : assignmentRepository.findByFaculty_IdAndActiveTrue(facultyId)) {
            cst.setActive(false);
            assignmentRepository.save(cst);
        }
    }

    private Faculty requireFaculty(Long schoolId, Long facultyId) {
        return facultyRepository.findByIdAndSchool_IdWithDetails(facultyId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
    }

    private FacultyProfile requireProfile(Faculty faculty) {
        return facultyProfileRepository.findByFaculty_Id(faculty.getId())
                .orElseGet(() -> {
                    FacultyProfile p = FacultyProfile.builder()
                            .faculty(faculty)
                            .accountStatus(FacultyAccountStatus.ACTIVE)
                            .joiningDate(LocalDate.now())
                            .build();
                    return facultyProfileRepository.save(p);
                });
    }

    private School assertSchool(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));
    }

    private void validateEmailUnique(String email, Long excludeId) {
        boolean exists = excludeId == null
                ? facultyRepository.existsByEmailIgnoreCase(email)
                : facultyRepository.existsByEmailIgnoreCaseAndIdNot(email, excludeId);
        if (exists) {
            throw new IllegalArgumentException("Email already registered");
        }
    }

    private Subject resolveSubject(Long subjectId, String specialization) {
        if (subjectId != null) {
            return subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        }
        String name = specialization != null ? specialization.trim() : "General";
        return subjectRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> subjectRepository.save(Subject.builder().name(name).build()));
    }

    private String resolveEmployeeId(String requested, String schoolCode) {
        if (requested != null && !requested.isBlank()) {
            if (facultyRepository.existsByFacultyIdIgnoreCase(requested.trim())) {
                throw new IllegalArgumentException("Employee ID already exists");
            }
            return requested.trim();
        }
        return generateFacultyCode(schoolCode);
    }

    private void applyFiles(
            FacultyProfile profile, Long schoolId,
            MultipartFile photo, MultipartFile resume, MultipartFile idProof,
            MultipartFile certificates, MultipartFile experienceLetters) {
        if (photo != null && !photo.isEmpty()) {
            validateImage(photo);
            profile.setPhotoUrl(fileStorageHelper.store(schoolId, "teacher-photos", photo));
        }
        if (resume != null && !resume.isEmpty()) {
            validateDocument(resume);
            profile.setResumePath(fileStorageHelper.store(schoolId, "teacher-documents", resume));
        }
        if (idProof != null && !idProof.isEmpty()) {
            validateDocument(idProof);
            profile.setIdProofPath(fileStorageHelper.store(schoolId, "teacher-documents", idProof));
        }
        if (certificates != null && !certificates.isEmpty()) {
            validateDocument(certificates);
            profile.setCertificatesPath(fileStorageHelper.store(schoolId, "teacher-documents", certificates));
        }
        if (experienceLetters != null && !experienceLetters.isEmpty()) {
            validateDocument(experienceLetters);
            profile.setExperienceLettersPath(fileStorageHelper.store(schoolId, "teacher-documents", experienceLetters));
        }
    }

    private static void validateImage(MultipartFile file) {
        if (file.getSize() > MAX_DOC_BYTES) {
            throw new IllegalArgumentException("Photo must not exceed 5MB");
        }
        String ext = extension(file.getOriginalFilename());
        if (!IMAGE_EXT.contains(ext)) {
            throw new IllegalArgumentException("Photo must be JPG or PNG");
        }
    }

    private static void validateDocument(MultipartFile file) {
        if (file.getSize() > MAX_DOC_BYTES) {
            throw new IllegalArgumentException("Document must not exceed 5MB");
        }
        String ext = extension(file.getOriginalFilename());
        if (!DOC_EXT.contains(ext)) {
            throw new IllegalArgumentException("Document must be PDF or Word");
        }
    }

    private static String extension(String name) {
        if (name == null) {
            return "";
        }
        int i = name.lastIndexOf('.');
        return i >= 0 ? name.substring(i + 1).toLowerCase() : "";
    }

    private static int computeAge(LocalDate dob) {
        if (dob == null) {
            return 30;
        }
        return Math.max(18, Period.between(dob, LocalDate.now()).getYears());
    }

    private static FacultyAccountStatus parseStatus(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            return null;
        }
        return FacultyAccountStatus.valueOf(status.trim().toUpperCase());
    }

    private static FacultyAccountStatus parseStatusRequired(String status) {
        return FacultyAccountStatus.valueOf(status.trim().toUpperCase());
    }

    private static FacultyEmploymentType parseEmploymentType(String type) {
        if (type == null || type.isBlank()) {
            return FacultyEmploymentType.FULL_TIME;
        }
        return FacultyEmploymentType.valueOf(type.trim().toUpperCase());
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static FilterResolution resolveFilters(String status, String statsFilter) {
        String sf = blankToNull(statsFilter);
        String st = blankToNull(status);
        if (sf != null && !"ALL".equalsIgnoreCase(sf) && !"TOTAL".equalsIgnoreCase(sf)) {
            if ("UNASSIGNED".equalsIgnoreCase(sf)) {
                FacultyAccountStatus dropdown = parseStatus(st);
                return new FilterResolution(dropdown, true, "UNASSIGNED", st != null ? st : "ALL");
            }
            FacultyAccountStatus cardStatus = FacultyAccountStatus.valueOf(sf.toUpperCase());
            FacultyAccountStatus effective = (st != null && !"ALL".equalsIgnoreCase(st))
                    ? parseStatusRequired(st) : cardStatus;
            return new FilterResolution(effective, false, sf.toUpperCase(), st != null ? st : sf.toUpperCase());
        }
        return new FilterResolution(parseStatus(st), false, "ALL", st != null ? st : "ALL");
    }

    private static String buildShowingLabel(int page, int size, long total) {
        if (total == 0) {
            return "Showing 0 staff members";
        }
        int from = page * size + 1;
        int to = (int) Math.min((long) (page + 1) * size, total);
        return "Showing " + from + " to " + to + " of " + total + " staff members";
    }

    private static String currentAcademicYear() {
        LocalDate now = LocalDate.now();
        int startYear = now.getMonthValue() >= 4 ? now.getYear() : now.getYear() - 1;
        return "AY " + startYear + "-" + (startYear + 1);
    }

    private record FilterResolution(
            FacultyAccountStatus status,
            boolean unassignedOnly,
            String statsFilterLabel,
            String statusDropdownLabel
    ) {
    }

    private record TeacherExportBundle(
            Faculty faculty,
            FacultyProfile profile,
            List<ClassSubjectTeacher> assignments
    ) {
    }

    private List<TeacherExportBundle> resolveTeachersForExport(AdminTeacherRequests.ExportRequest request) {
        if (request.getFacultyIds() != null && !request.getFacultyIds().isEmpty()) {
            return request.getFacultyIds().stream()
                    .map(id -> loadExportBundle(request.getSchoolId(), id))
                    .toList();
        }
        FilterResolution filters = resolveFilters(request.getStatus(), request.getStatsFilter());
        Page<Faculty> page = facultyRepository.searchBySchool(
                request.getSchoolId(),
                blankToNull(request.getQ()),
                request.getSubjectId(),
                filters.status(),
                filters.unassignedOnly(),
                PageRequest.of(0, 10_000));
        List<Long> ids = page.getContent().stream().map(Faculty::getId).toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, List<ClassSubjectTeacher>> assignments = assignmentRepository.findActiveByFacultyIds(ids).stream()
                .collect(Collectors.groupingBy(cst -> cst.getFaculty().getId()));
        return page.getContent().stream()
                .map(f -> {
                    FacultyProfile profile = facultyProfileRepository.findByFaculty_Id(f.getId()).orElse(null);
                    return new TeacherExportBundle(
                            f,
                            profile,
                            assignments.getOrDefault(f.getId(), List.of()));
                })
                .toList();
    }

    private TeacherExportBundle loadExportBundle(Long schoolId, Long facultyId) {
        Faculty faculty = requireFaculty(schoolId, facultyId);
        FacultyProfile profile = facultyProfileRepository.findByFaculty_Id(facultyId).orElse(null);
        List<ClassSubjectTeacher> assignments = assignmentRepository.findByFaculty_IdAndActiveTrueWithSubject(facultyId);
        return new TeacherExportBundle(faculty, profile, assignments);
    }

    private Map<String, String> toExportMap(TeacherExportBundle b, List<String> fields, List<String> headers) {
        Map<String, String> values = buildExportValues(b);
        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            row.put(headers.get(i), values.getOrDefault(fields.get(i), ""));
        }
        return row;
    }

    private Map<String, String> buildExportValues(TeacherExportBundle b) {
        Faculty f = b.faculty();
        FacultyProfile p = b.profile();
        List<ClassSubjectTeacher> assignments = b.assignments();
        String classLabels = assignments.stream()
                .map(a -> a.getClassName() + "-" + a.getSection())
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));
        String subjectLabels = assignments.stream()
                .map(a -> a.getSubject().getName())
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));

        Map<String, String> m = new LinkedHashMap<>();
        m.put("teacherName", f.getFirstName() + " " + f.getLastName());
        m.put("employeeId", f.getFacultyId());
        m.put("gender", f.getGender());
        m.put("dob", p != null && p.getDateOfBirth() != null ? p.getDateOfBirth().toString() : "");
        m.put("phone", f.getPhone());
        m.put("email", f.getEmail());
        m.put("address", f.getAddress());
        m.put("profilePhoto", p != null && p.getPhotoUrl() != null ? p.getPhotoUrl() : "");
        m.put("subjectSpecialization", f.getSubject().getName());
        m.put("qualification", f.getQualification());
        m.put("experience", f.getExperience() != null ? f.getExperience() + " years" : "");
        m.put("certifications", p != null && p.getCertifications() != null ? p.getCertifications() : "");
        m.put("skills", p != null && p.getSkills() != null ? p.getSkills() : "");
        m.put("joiningDate", p != null && p.getJoiningDate() != null ? p.getJoiningDate().toString() : "");
        m.put("employmentType", p != null && p.getEmploymentType() != null ? p.getEmploymentType().name() : "");
        m.put("status", p != null ? p.getAccountStatus().name() : FacultyAccountStatus.ACTIVE.name());
        m.put("assignedClasses", classLabels);
        m.put("assignedSubjects", subjectLabels.isBlank() ? f.getSubject().getName() : subjectLabels);
        m.put("department", p != null && p.getDepartment() != null ? p.getDepartment() : "");
        m.put("salary", "");
        m.put("idProof", p != null && p.getIdProofPath() != null ? p.getIdProofPath() : "");
        m.put("certificates", p != null && p.getCertifications() != null ? p.getCertifications() : "");
        m.put("resume", p != null && p.getResumePath() != null ? p.getResumePath() : "");
        m.put("contract", "");
        m.put("otherDocuments", "");
        return m;
    }

    private List<String> resolveExportFields(Map<String, List<String>> fields) {
        if (fields == null || fields.isEmpty()) {
            return List.of("teacherName", "employeeId", "email", "subjectSpecialization", "assignedClasses", "status");
        }
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        for (List<String> group : fields.values()) {
            if (group != null) {
                ordered.addAll(group);
            }
        }
        return new ArrayList<>(ordered);
    }

    private String generateFacultyCode(String schoolCode) {
        String prefix = "T";
        String code;
        int seq = 1;
        do {
            code = prefix + "-" + LocalDate.now().getYear() + "-" + String.format("%03d", seq++);
        } while (facultyRepository.existsByFacultyIdIgnoreCase(code) && seq < 10000);
        return code;
    }

    private String[] splitName(String fullName) {
        String trimmed = fullName.trim();
        int idx = trimmed.lastIndexOf(' ');
        if (idx <= 0) {
            return new String[]{trimmed, "."};
        }
        return new String[]{trimmed.substring(0, idx).trim(), trimmed.substring(idx + 1).trim()};
    }
}
