package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.TeacherMessageRequest;
import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.Faculty;
import com.org.careerbuilder.models.StudyMaterial;
import com.org.careerbuilder.repository.FacultyRepository;
import com.org.careerbuilder.repository.StudentRepository;
import com.org.careerbuilder.repository.StudyMaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentClassesServiceImpl implements StudentClassesService {
    private final StudyMaterialRepository studyMaterialRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    @Override
    public StudentClassesOverviewResponse getOverview(Long studentId, LocalDate date) {
        // ...existing code...
        int totalClasses = 6;
        int completedClasses = 4;
        int totalHours = 5;
        String breakHours = "1h 30m";
        int specialClasses = 1;
        return new StudentClassesOverviewResponse(totalClasses, completedClasses, totalHours, breakHours, specialClasses);
    }

    @Override
    public List<StudentClassCardResponse> getTodayClasses(Long studentId, LocalDate date) {
        // ...existing code...
        return Collections.emptyList();
    }

    @Override
    public List<StudentClassCardResponse> getSubjects(Long studentId) {
        // ...existing code...
        return Collections.emptyList();
    }

    @Override
    public List<StudentCalendarSlotResponse> getCalendar(Long studentId, LocalDate date) {
        // ...existing code...
        return Collections.emptyList();
    }

    @Override
    public List<StudyMaterialCardResponse> getStudyMaterials(Long studentId, Long subjectId) {
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
        // ...existing code...
        return Collections.emptyList();
    }

    @Override
    public TeacherProfileResponse getTeacherProfile(Long teacherId) {
        Faculty teacher = facultyRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));
        List<String> classesTaught = getClassesTaughtByTeacher(teacherId);
        TeacherProfileResponse.OfficeHoursDTO officeHours = new TeacherProfileResponse.OfficeHoursDTO(
                "Mon - Fri", "3:30 PM", "4:30 PM");
        String teacherName = teacher.getFirstName() + " " + teacher.getLastName();
        String subjectName = teacher.getSubject() != null ? teacher.getSubject().getName() : "N/A";
        return new TeacherProfileResponse(
                teacher.getId(),
                teacherName,
                subjectName,
                getTeacherPhotoUrl(teacherId),
                teacher.getQualification(),
                teacher.getExperience(),
                getTeacherBio(teacherId),
                teacher.getEmail(),
                teacher.getPhone(),
                classesTaught,
                officeHours,
                calculateAvailabilityStatus(teacher),
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

    private List<String> getClassesTaughtByTeacher(Long teacherId) {
        return List.of("Class 10 A", "Class 10 B", "Class 11 A");
    }

    private String getTeacherPhotoUrl(Long teacherId) {
        return "https://api.example.com/teachers/" + teacherId + "/photo";
    }

    private String getTeacherBio(Long teacherId) {
        return "Dedicated educator with passion for teaching. Experienced in making complex subjects easy to understand.";
    }

    private String calculateAvailabilityStatus(Faculty teacher) {
        return "Available to query";
    }
}

