package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.TeacherMessageRequest;
import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.models.StudyMaterial;
import com.org.careerbuilder.repository.StudentRepository;
import com.org.careerbuilder.repository.StudyMaterialRepository;
import com.org.careerbuilder.service.StudentClassesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentClassesServiceImpl implements StudentClassesService {

    private final StudyMaterialRepository studyMaterialRepository;

    private final StudentRepository studentRepository;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    /**
     * Overview summary shown at top of Student Classes dashboard
     */
    @Override
    public StudentClassesOverviewResponse getOverview(Long studentId, LocalDate date) {

        // Dummy values for now (can be replaced with DB queries later)
        int totalClasses = 6;
        int completedClasses = 4;
        int totalHours = 5;
        String breakHours = "1h 30m";
        int specialClasses = 1;

        return new StudentClassesOverviewResponse(
                totalClasses,
                completedClasses,
                totalHours,
                breakHours,
                specialClasses
        );
    }

    /**
     * Fetch today's classes for a student
     */
    @Override
    public List<StudentClassCardResponse> getTodayClasses(Long studentId, LocalDate date) {

        // Placeholder until ClassSchedule table is implemented
        return Collections.emptyList();
    }

    /**
     * Fetch subjects for the student
     */
    @Override
    public List<StudentClassCardResponse> getSubjects(Long studentId) {

        // Placeholder until Subject mapping is implemented
        return Collections.emptyList();
    }

    /**
     * Calendar view for classes
     */
    @Override
    public List<StudentCalendarSlotResponse> getCalendar(Long studentId, LocalDate date) {

        // Placeholder until timetable feature is implemented
        return Collections.emptyList();
    }

    /**
     * Fetch study materials filtered by subject
     */
    @Override
    public List<StudyMaterialCardResponse> getStudyMaterials(Long studentId, Long subjectId) {

        List<StudyMaterial> materials;

        if (subjectId != null) {
            materials = studyMaterialRepository
                    .findBySubject_IdOrderByUploadedAtDesc(subjectId);
        } else {
            materials = studyMaterialRepository
                    .findAllByOrderByUploadedAtDesc();
        }

        return materials.stream()
                .map(this::mapToCardResponse)
                .toList();
    }

    /**
     * Convert StudyMaterial entity → UI card response
     */
    private StudyMaterialCardResponse mapToCardResponse(StudyMaterial material) {

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

    /**
     * Fetch teachers for a subject
     */
    @Override
    public List<TeacherCardResponse> getTeachers(Long studentId, Long subjectId) {

        // Placeholder until Teacher-Subject mapping exists
        return Collections.emptyList();
    }

    /**
     * Send message to teacher
     */
    @Override
    public void sendTeacherMessage(Long studentId, Long teacherId, TeacherMessageRequest request) {

        // In future this will be saved to messages table
        System.out.println(
                "Message from Student " + studentId +
                        " to Teacher " + teacherId +
                        " : " + request.message()
        );
    }
}