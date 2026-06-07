package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.TeacherScheduleEntryRequest;
import com.org.careerbuilder.dto.response.TeacherScheduleDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.TeacherActivityType;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.TeacherScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TeacherScheduleServiceImpl implements TeacherScheduleService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final FacultyRepository facultyRepository;
    private final TeacherScheduleEntryRepository teacherScheduleEntryRepository;
    private final ClassSubjectTeacherRepository classSubjectTeacherRepository;
    private final ClassScheduleSlotRepository classScheduleSlotRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;

    private Faculty loadFaculty(Long facultyId) {
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
    }

    private static LocalDate mondayOfWeekContaining(LocalDate any) {
        return any.minusDays((any.getDayOfWeek().getValue() + 6) % 7);
    }

    private static String statusFor(LocalDate day, LocalTime start, LocalTime end) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime st = day.atTime(start);
        LocalDateTime en = day.atTime(end);
        if (now.isBefore(st)) {
            return "UPCOMING";
        }
        if (!now.isBefore(en)) {
            return "COMPLETED";
        }
        return "ONGOING";
    }

    private static String timeRange(LocalTime start, LocalTime end) {
        return TIME_FMT.format(start) + " - " + TIME_FMT.format(end);
    }

    private static String activityFromSlot(ClassScheduleSlot.SlotType slotType) {
        return switch (slotType) {
            case CLASS -> TeacherActivityType.CLASS.name();
            case BREAK, LUNCH -> TeacherActivityType.BREAK.name();
            case SPECIAL -> TeacherActivityType.OTHER.name();
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private static boolean isClassFilterProvided(String className, String section) {
        return hasText(className) || hasText(section);
    }

    private List<ClassSubjectTeacher> resolveAssignmentsForView(Long facultyId, Long schoolId, String className, String section) {
        List<ClassSubjectTeacher> assignments = classSubjectTeacherRepository.findByFaculty_IdAndActiveTrue(facultyId);
        if (!isClassFilterProvided(className, section)) {
            return assignments;
        }
        if (!hasText(className) || !hasText(section)) {
            throw new IllegalArgumentException("Both className and section are required when filtering by class");
        }
        List<ClassSubjectTeacher> filtered = assignments.stream()
                .filter(cst -> cst.getSchoolId().equals(schoolId)
                        && cst.getClassName().equalsIgnoreCase(className.trim())
                        && cst.getSection().equalsIgnoreCase(section.trim()))
                .toList();
        if (filtered.isEmpty()) {
            throw new IllegalArgumentException("Teacher is not assigned to the selected class/section");
        }
        return filtered;
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherScheduleDtos.ScheduleFiltersResponse getScheduleFilters(Long facultyId) {
        loadFaculty(facultyId);
        List<TeacherScheduleDtos.ClassSectionOption> classes = classSubjectTeacherRepository
                .findByFaculty_IdAndActiveTrue(facultyId)
                .stream()
                .map(cst -> new TeacherScheduleDtos.ClassSectionOption(
                        cst.getClassName(),
                        cst.getSection(),
                        "Grade " + cst.getClassName() + " " + cst.getSection()
                ))
                .distinct()
                .sorted(Comparator.comparing(TeacherScheduleDtos.ClassSectionOption::className)
                        .thenComparing(TeacherScheduleDtos.ClassSectionOption::section))
                .toList();
        return new TeacherScheduleDtos.ScheduleFiltersResponse(classes);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherScheduleDtos.DayScheduleResponse getDaySchedule(Long facultyId, LocalDate date, String className, String section) {
        Faculty faculty = loadFaculty(facultyId);
        int dow = date.getDayOfWeek().getValue();
        Long schoolId = faculty.getSchool().getId();
        List<ClassSubjectTeacher> assignments = resolveAssignmentsForView(facultyId, schoolId, className, section);

        List<TeacherScheduleDtos.ScheduleItemResponse> items = new ArrayList<>();

        for (TeacherScheduleEntry e : teacherScheduleEntryRepository.findApplicableForDate(facultyId, date, dow)) {
            if (isClassFilterProvided(className, section) && (!hasText(e.getClassName()) || !hasText(e.getSection())
                    || !e.getClassName().equalsIgnoreCase(className.trim())
                    || !e.getSection().equalsIgnoreCase(section.trim()))) {
                continue;
            }
            items.add(fromTeacherEntry(e, date, schoolId));
        }

        for (ClassSubjectTeacher cst : assignments) {
            List<ClassScheduleSlot> slots = classScheduleSlotRepository
                    .findBySchoolIdAndClassNameAndSectionAndSubject_IdAndDayOfWeekAndActiveTrueOrderByStartTime(
                            schoolId, cst.getClassName(), cst.getSection(), cst.getSubject().getId(), dow);
            for (ClassScheduleSlot slot : slots) {
                items.add(fromSlot(slot, date, cst.getClassName(), cst.getSection(), cst.getSubject(), schoolId));
            }
        }

        items.sort(Comparator.comparing(TeacherScheduleDtos.ScheduleItemResponse::startTime));
        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return new TeacherScheduleDtos.DayScheduleResponse(date, dayName, items);
    }

    private Integer resolveStudentCount(Long schoolId, String className, String section) {
        if (schoolId == null || !hasText(className) || !hasText(section)) {
            return null;
        }
        int n = (int) studentRepository.countBySchool_IdAndClassNameAndSection(schoolId, className.trim(), section.trim());
        return n;
    }

    private TeacherScheduleDtos.ScheduleItemResponse fromTeacherEntry(TeacherScheduleEntry e, LocalDate date, Long schoolId) {
        boolean showClass = e.getActivityType() == TeacherActivityType.CLASS
                && e.getClassName() != null && !e.getClassName().isBlank()
                && e.getSection() != null && !e.getSection().isBlank();
        String secLabel = e.getSectionLabel() != null && !e.getSectionLabel().isBlank()
                ? e.getSectionLabel()
                : (e.getSection() != null ? "Section " + e.getSection() : null);
        Integer headcount = (showClass && schoolId != null)
                ? resolveStudentCount(schoolId, e.getClassName(), e.getSection())
                : null;
        return new TeacherScheduleDtos.ScheduleItemResponse(
                "TEACHER_ENTRY",
                e.getId(),
                e.getTitle(),
                e.getActivityType().name(),
                date,
                e.getStartTime(),
                e.getEndTime(),
                timeRange(e.getStartTime(), e.getEndTime()),
                secLabel,
                e.getVenue(),
                showClass,
                e.getClassName(),
                e.getSection(),
                statusFor(date, e.getStartTime(), e.getEndTime()),
                e.getNotes(),
                headcount
        );
    }

    private TeacherScheduleDtos.ScheduleItemResponse fromSlot(
            ClassScheduleSlot slot,
            LocalDate date,
            String className,
            String section,
            Subject subject,
            Long schoolId) {
        String title = slot.getTitle() != null && !slot.getTitle().isBlank()
                ? slot.getTitle()
                : (subject != null ? subject.getName() : "Class");
        String secLabel = "Section " + section;
        boolean show = slot.getSlotType() == ClassScheduleSlot.SlotType.CLASS;
        Integer headcount = (show && schoolId != null)
                ? resolveStudentCount(schoolId, className, section)
                : null;
        return new TeacherScheduleDtos.ScheduleItemResponse(
                "CLASS_TIMETABLE",
                slot.getId(),
                title,
                activityFromSlot(slot.getSlotType()),
                date,
                slot.getStartTime(),
                slot.getEndTime(),
                timeRange(slot.getStartTime(), slot.getEndTime()),
                secLabel,
                null,
                show,
                className,
                section,
                statusFor(date, slot.getStartTime(), slot.getEndTime()),
                null,
                headcount
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherScheduleDtos.WeekScheduleResponse getWeekSchedule(Long facultyId, LocalDate weekStartMonday, String className, String section) {
        LocalDate start = weekStartMonday != null ? weekStartMonday : mondayOfWeekContaining(LocalDate.now());
        List<TeacherScheduleDtos.DayScheduleResponse> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            days.add(getDaySchedule(facultyId, start.plusDays(i), className, section));
        }
        return new TeacherScheduleDtos.WeekScheduleResponse(days);
    }

    private void validateRequest(TeacherScheduleEntryRequest req) {
        if (req.getStartTime() == null || req.getEndTime() == null || !req.getStartTime().isBefore(req.getEndTime())) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
        if (req.getActivityType() == TeacherActivityType.CLASS) {
            if (req.getClassName() == null || req.getClassName().isBlank()
                    || req.getSection() == null || req.getSection().isBlank()) {
                throw new IllegalArgumentException("Class activity requires className and section");
            }
        }
        if (req.isRecurring()) {
            if (req.getDayOfWeek() == null || req.getDayOfWeek() < 1 || req.getDayOfWeek() > 7) {
                throw new IllegalArgumentException("Recurring activities require dayOfWeek between 1 (Mon) and 7 (Sun)");
            }
        } else {
            if (req.getSpecificDate() == null) {
                throw new IllegalArgumentException("One-off activities require specificDate");
            }
        }
    }

    private void assertClassAssignmentAllowed(Long facultyId, Long schoolId, TeacherScheduleEntryRequest request) {
        String className = request.getClassName();
        String section = request.getSection();
        if (className == null || className.isBlank() || section == null || section.isBlank()) {
            return;
        }
        List<ClassSubjectTeacher> assignments = request.getSubjectId() != null
                ? classSubjectTeacherRepository.findBySchoolIdAndClassNameAndSectionAndSubject_IdAndActiveTrue(
                        schoolId, className, section, request.getSubjectId())
                : classSubjectTeacherRepository.findBySchoolIdAndClassNameAndSectionAndActiveTrue(schoolId, className, section);
        boolean assigned = assignments.stream().anyMatch(cst -> cst.getFaculty().getId().equals(facultyId));
        if (!assigned) {
            throw new IllegalArgumentException("Teacher is not assigned to the selected class/section"
                    + (request.getSubjectId() != null ? " for the selected subject" : ""));
        }
    }

    private void assertNoOverlap(Long facultyId, LocalDate date, int dow, LocalTime start, LocalTime end, Long excludeId) {
        long n = teacherScheduleEntryRepository.countOverlapping(facultyId, date, dow, start, end, excludeId);
        if (n > 0) {
            throw new IllegalStateException("This time overlaps another activity for the same day");
        }
    }

    @Override
    @Transactional
    public TeacherScheduleDtos.ScheduleEntryResponse createEntry(Long facultyId, TeacherScheduleEntryRequest request) {
        validateRequest(request);
        Faculty faculty = loadFaculty(facultyId);
        LocalDate probe = request.isRecurring()
                ? LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.of(request.getDayOfWeek())))
                : request.getSpecificDate();
        int dow = probe.getDayOfWeek().getValue();
        assertClassAssignmentAllowed(facultyId, faculty.getSchool().getId(), request);
        assertNoOverlap(facultyId, probe, dow, request.getStartTime(), request.getEndTime(), null);

        Subject subject = null;
        if (request.getSubjectId() != null) {
            subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        }

        TeacherScheduleEntry e = TeacherScheduleEntry.builder()
                .faculty(faculty)
                .schoolId(faculty.getSchool().getId())
                .title(request.getTitle().trim())
                .activityType(request.getActivityType())
                .recurring(request.isRecurring())
                .dayOfWeek(request.isRecurring() ? request.getDayOfWeek() : null)
                .specificDate(request.isRecurring() ? null : request.getSpecificDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .className(request.getClassName())
                .section(request.getSection())
                .sectionLabel(request.getSectionLabel())
                .venue(request.getVenue())
                .notes(request.getNotes())
                .substituteForName(trimToNull(request.getSubstituteForName()))
                .subject(subject)
                .build();
        TeacherScheduleEntry saved = teacherScheduleEntryRepository.save(e);
        return toEntryResponse(saved);
    }

    @Override
    @Transactional
    public TeacherScheduleDtos.ScheduleEntryResponse updateEntry(Long facultyId, Long entryId, TeacherScheduleEntryRequest request) {
        validateRequest(request);
        TeacherScheduleEntry e = teacherScheduleEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule entry not found"));
        if (!e.getFaculty().getId().equals(facultyId)) {
            throw new IllegalArgumentException("Entry belongs to another teacher");
        }
        Long schoolId = e.getSchoolId();
        LocalDate probe = request.isRecurring()
                ? LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.of(request.getDayOfWeek())))
                : request.getSpecificDate();
        int dow = probe.getDayOfWeek().getValue();
        assertClassAssignmentAllowed(facultyId, schoolId, request);
        assertNoOverlap(facultyId, probe, dow, request.getStartTime(), request.getEndTime(), entryId);

        Subject subject = null;
        if (request.getSubjectId() != null) {
            subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        }

        e.setTitle(request.getTitle().trim());
        e.setActivityType(request.getActivityType());
        e.setRecurring(request.isRecurring());
        e.setDayOfWeek(request.isRecurring() ? request.getDayOfWeek() : null);
        e.setSpecificDate(request.isRecurring() ? null : request.getSpecificDate());
        e.setStartTime(request.getStartTime());
        e.setEndTime(request.getEndTime());
        e.setClassName(request.getClassName());
        e.setSection(request.getSection());
        e.setSectionLabel(request.getSectionLabel());
        e.setVenue(request.getVenue());
        e.setNotes(request.getNotes());
        e.setSubject(subject);
        return toEntryResponse(teacherScheduleEntryRepository.save(e));
    }

    private TeacherScheduleDtos.ScheduleEntryResponse toEntryResponse(TeacherScheduleEntry e) {
        return new TeacherScheduleDtos.ScheduleEntryResponse(
                e.getId(),
                e.getTitle(),
                e.getActivityType().name(),
                e.isRecurring(),
                e.getDayOfWeek(),
                e.getSpecificDate(),
                e.getStartTime(),
                e.getEndTime(),
                e.getClassName(),
                e.getSection(),
                e.getSectionLabel(),
                e.getVenue(),
                e.getNotes(),
                e.getSubject() != null ? e.getSubject().getId() : null,
                e.getSubstituteForName()
        );
    }

    @Override
    @Transactional
    public void deleteEntry(Long facultyId, Long entryId) {
        TeacherScheduleEntry e = teacherScheduleEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule entry not found"));
        if (!e.getFaculty().getId().equals(facultyId)) {
            throw new IllegalArgumentException("Entry belongs to another teacher");
        }
        teacherScheduleEntryRepository.delete(e);
    }
}
