package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.TeacherMessageRequest;
import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.ClassScheduleSlot;
import com.org.careerbuilder.models.ClassSession;
import com.org.careerbuilder.models.ClassSubjectTeacher;
import com.org.careerbuilder.models.Faculty;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.models.StudyMaterial;
import com.org.careerbuilder.models.enums.SessionStatus;
import com.org.careerbuilder.models.enums.SessionType;
import com.org.careerbuilder.repository.ClassScheduleSlotRepository;
import com.org.careerbuilder.repository.ClassSessionRepository;
import com.org.careerbuilder.repository.ClassSubjectTeacherRepository;
import com.org.careerbuilder.repository.FacultyRepository;
import com.org.careerbuilder.repository.MyClassRepository;
import com.org.careerbuilder.repository.StudentRepository;
import com.org.careerbuilder.repository.StudyMaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentClassesServiceImpl implements StudentClassesService {
    private final StudyMaterialRepository studyMaterialRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final MyClassRepository myClassRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ClassScheduleSlotRepository classScheduleSlotRepository;
    private final ClassSubjectTeacherRepository classSubjectTeacherRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    @Override
    public StudentClassesOverviewResponse getOverview(Long studentId, LocalDate date) {
        Student student = getStudentOrThrow(studentId);

        List<ClassSession> sessions = classSessionRepository
                .findByClassNameAndSectionAndSessionDateOrderByStartTimeAsc(
                        student.getClassName(),
                        student.getSection(),
                        date
                );

        int totalClasses = (int) sessions.stream()
                .filter(session -> isTeachingSession(session.getSessionType()))
                .count();

        int completedClasses = (int) sessions.stream()
                .filter(session -> session.getStatus() == SessionStatus.COMPLETED)
                .count();

        int totalHours = sessions.stream()
                .filter(session -> isTeachingSession(session.getSessionType()))
                .mapToInt(session -> (int) java.time.Duration.between(session.getStartTime(), session.getEndTime()).toHours())
                .sum();

        long breakMinutes = sessions.stream()
                .filter(session -> session.getSessionType() == SessionType.BREAK || session.getSessionType() == SessionType.LUNCH)
                .mapToLong(session -> java.time.Duration.between(session.getStartTime(), session.getEndTime()).toMinutes())
                .sum();

        int specialClasses = (int) sessions.stream()
                .filter(session -> session.getSessionType() == SessionType.SPECIAL)
                .count();

        return new StudentClassesOverviewResponse(
                totalClasses,
                completedClasses,
                totalHours,
                formatDurationText(breakMinutes),
                specialClasses
        );
    }

    @Override
    public List<StudentClassCardResponse> getTodayClasses(Long studentId, LocalDate date) {
        Student student = getStudentOrThrow(studentId);

        List<ClassSession> sessions = classSessionRepository
                .findByClassNameAndSectionAndSessionDateOrderByStartTimeAsc(
                        student.getClassName(),
                        student.getSection(),
                        date
                );

        return sessions.stream()
                .filter(session -> isTeachingSession(session.getSessionType()))
                .map(this::mapSessionToClassCard)
                .toList();
    }

    @Override
    public List<StudentClassCardResponse> getSubjects(Long studentId) {
        List<Object[]> rows = myClassRepository.findSubjectCardsByStudentId(studentId);

        return rows.stream()
            .map(row -> new StudentClassCardResponse(
                row[0] != null ? ((Number) row[0]).longValue() : null,
                row[1] != null ? String.valueOf(row[1]) : "Unknown Subject",
                row[3] != null ? "Section " + row[3] : "Section not available",
                "Not assigned",
                "Time not scheduled",
                null,
                0,
                "JOIN"
            ))
            .toList();
    }

    @Override
    public List<StudentCalendarSlotResponse> getCalendar(Long studentId, LocalDate date) {
        Student student = getStudentOrThrow(studentId);

        int dayOfWeek = date.getDayOfWeek().getValue();
        List<ClassScheduleSlot> slots = classScheduleSlotRepository
                .findBySchoolIdAndClassNameAndSectionAndDayOfWeekAndActiveTrueOrderByStartTime(
                        student.getSchool().getId(),
                        student.getClassName(),
                        student.getSection(),
                        dayOfWeek
                );

        if (slots.isEmpty()) {
            List<ClassSession> sessions = classSessionRepository
                    .findByClassNameAndSectionAndSessionDateOrderByStartTimeAsc(
                            student.getClassName(),
                            student.getSection(),
                            date
                    );

            return sessions.stream()
                    .map(session -> new StudentCalendarSlotResponse(
                            formatTimeRange(session.getStartTime(), session.getEndTime()),
                            session.getSubject() != null ? session.getSubject().getName() : "General",
                            session.getTopic() != null ? session.getTopic() : "Class Session",
                            session.getSessionType().name()
                    ))
                    .toList();
        }

        return slots.stream()
                .map(slot -> new StudentCalendarSlotResponse(
                        formatTimeRange(slot.getStartTime(), slot.getEndTime()),
                        slot.getSubject() != null ? slot.getSubject().getName() : "General",
                        slot.getTitle() != null ? slot.getTitle() : defaultTitleForSlot(slot),
                        slot.getSlotType().name()
                ))
                .toList();
    }

    @Override
    public List<StudyMaterialCardResponse> getStudyMaterials(Long studentId, Long subjectId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        List<StudyMaterial> materials;
        if (subjectId != null) {
            materials = studyMaterialRepository.findBySubject_IdOrderByUploadedAtDesc(subjectId);
        } else {
            materials = studyMaterialRepository.findAllByOrderByUploadedAtDesc();
        }
        return materials.stream().map(this::mapToMaterialCardResponse).toList();
    }

    @Override
    public List<TeacherCardResponse> getTeachers(Long studentId, Long subjectId) {
        Student student = getStudentOrThrow(studentId);

        List<ClassSubjectTeacher> mappings;
        if (subjectId != null) {
            mappings = classSubjectTeacherRepository.findBySchoolIdAndClassNameAndSectionAndSubject_IdAndActiveTrue(
                    student.getSchool().getId(),
                    student.getClassName(),
                    student.getSection(),
                    subjectId
            );
        } else {
            mappings = classSubjectTeacherRepository.findBySchoolIdAndClassNameAndSectionAndActiveTrue(
                    student.getSchool().getId(),
                    student.getClassName(),
                    student.getSection()
            );
        }

        Map<Long, List<ClassSubjectTeacher>> byFaculty = mappings.stream()
                .filter(mapping -> mapping.getFaculty() != null)
                .collect(Collectors.groupingBy(mapping -> mapping.getFaculty().getId()));

        return byFaculty.values().stream()
                .map(this::mapToTeacherCard)
                .sorted(Comparator.comparing(TeacherCardResponse::teacherName))
                .toList();
    }

    @Override
    public TeacherProfileResponse getTeacherProfile(Long teacherId) {
        Faculty teacher = facultyRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

        List<ClassSubjectTeacher> assignments = classSubjectTeacherRepository.findAll().stream()
                .filter(assignment -> assignment.getFaculty() != null
                        && teacherId.equals(assignment.getFaculty().getId())
                        && assignment.isActive())
                .toList();

        List<String> classesTaught = assignments.stream()
                .map(assignment -> assignment.getClassName() + " " + assignment.getSection())
                .distinct()
                .sorted()
                .map(value -> "Class " + value)
                .toList();

        TeacherProfileResponse.OfficeHoursDTO officeHours = new TeacherProfileResponse.OfficeHoursDTO(
                "Mon - Fri", "3:30 PM", "4:30 PM"
        );

        String teacherName = teacher.getFirstName() + " " + teacher.getLastName();
        String subjectName = teacher.getSubject() != null ? teacher.getSubject().getName() : "N/A";

        return new TeacherProfileResponse(
                teacher.getId(),
                teacherName,
                subjectName,
                getTeacherPhotoUrl(teacherId),
                teacher.getQualification(),
                teacher.getExperience(),
                getTeacherBio(teacher),
                teacher.getEmail(),
                teacher.getPhone(),
                classesTaught,
                officeHours,
                calculateAvailabilityStatus(),
                true
        );
    }

    @Override
    public TeacherMessageResponse sendTeacherMessage(Long studentId, Long teacherId, TeacherMessageRequest request) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        facultyRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));
        String sentAt = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        return new TeacherMessageResponse(1L, request.subject(), request.message(), sentAt, "SENT");
    }

    private Student getStudentOrThrow(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
    }

    private StudentClassCardResponse mapSessionToClassCard(ClassSession session) {
        String subjectName = session.getSubject() != null ? session.getSubject().getName() : "General";
        String teacherName = session.getTeacher() != null ? session.getTeacher().getName() : "Not assigned";

        int progressPercent = switch (session.getStatus()) {
            case COMPLETED -> 100;
            case SCHEDULED -> 0;
            case CANCELLED -> 0;
        };

        String actionText = switch (session.getStatus()) {
            case COMPLETED -> "COMPLETED";
            case SCHEDULED -> "JOIN";
            case CANCELLED -> "CANCELLED";
        };

        return new StudentClassCardResponse(
                session.getSubject() != null ? session.getSubject().getId() : null,
                subjectName,
                session.getTopic(),
                teacherName,
                formatTimeRange(session.getStartTime(), session.getEndTime()),
                session.getEnrolledCount(),
                progressPercent,
                actionText
        );
    }

    private TeacherCardResponse mapToTeacherCard(List<ClassSubjectTeacher> teacherMappings) {
        ClassSubjectTeacher first = teacherMappings.get(0);
        Faculty faculty = first.getFaculty();

        String classesText = teacherMappings.stream()
                .map(mapping -> "Class " + mapping.getClassName() + " " + mapping.getSection())
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));

        Integer experience = faculty.getExperience();
        int experienceYears = experience != null ? experience : 0;

        return new TeacherCardResponse(
                faculty.getId(),
                faculty.getFirstName() + " " + faculty.getLastName(),
                first.getSubject() != null ? first.getSubject().getName() : "N/A",
                experienceYears,
                faculty.getQualification(),
                calculateAvailabilityStatus(),
                classesText
        );
    }

    private StudyMaterialCardResponse mapToMaterialCardResponse(StudyMaterial material) {
        return new StudyMaterialCardResponse(
                material.getId(),
                material.getSubject().getId(),
                material.getSubject().getName(),
                material.getTitle(),
                material.getMaterialType().name(),
                material.getPages(),
                material.getUploadedBy(),
                material.getUploadedAt().format(DATE_FORMATTER)
        );
    }

    private boolean isTeachingSession(SessionType type) {
        return type == SessionType.REGULAR || type == SessionType.SPECIAL;
    }

    private String formatDurationText(long minutes) {
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        if (hours == 0) {
            return remainingMinutes + "m";
        }

        if (remainingMinutes == 0) {
            return hours + "h";
        }

        return hours + "h " + remainingMinutes + "m";
    }

    private String formatTimeRange(LocalTime startTime, LocalTime endTime) {
        return TIME_FORMATTER.format(startTime) + " - " + TIME_FORMATTER.format(endTime);
    }

    private String defaultTitleForSlot(ClassScheduleSlot slot) {
        if (slot.getSlotType() == ClassScheduleSlot.SlotType.BREAK) {
            return "Short Break";
        }
        if (slot.getSlotType() == ClassScheduleSlot.SlotType.LUNCH) {
            return "Lunch Break";
        }
        return slot.getSubject() != null ? slot.getSubject().getName() + " Session" : "Class Session";
    }

    private String getTeacherPhotoUrl(Long teacherId) {
        return "https://api.example.com/teachers/" + teacherId + "/photo";
    }

    private String getTeacherBio(Faculty teacher) {
        return "Dedicated educator in " +
                (teacher.getSubject() != null ? teacher.getSubject().getName() : "their subject") +
                " with a strong focus on student learning outcomes.";
    }

    private String calculateAvailabilityStatus() {
        return "Available to query";
    }
}
