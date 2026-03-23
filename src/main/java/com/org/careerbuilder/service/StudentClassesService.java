package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.TeacherMessageRequest;
import com.org.careerbuilder.dto.response.*;

import java.time.LocalDate;
import java.util.List;

public interface StudentClassesService {

    StudentClassesOverviewResponse getOverview(Long studentId, LocalDate date);

    List<StudentClassCardResponse> getTodayClasses(Long studentId, LocalDate date);

    List<StudentClassCardResponse> getSubjects(Long studentId);

    List<StudentCalendarSlotResponse> getCalendar(Long studentId, LocalDate date);

    List<StudyMaterialCardResponse> getStudyMaterials(Long studentId, Long subjectId);

    /**
     * Get all teachers assigned to student's classes
     * Can be filtered by subject
     */
    List<TeacherCardResponse> getTeachers(Long studentId, Long subjectId);

    /**
     * Get detailed profile information for a teacher
     * Includes bio, qualifications, experience, office hours, contact info
     */
    TeacherProfileResponse getTeacherProfile(Long teacherId);

    /**
     * Send a message from student to teacher
     * Creates message record and returns confirmation with metadata
     */
    TeacherMessageResponse sendTeacherMessage(Long studentId, Long teacherId, TeacherMessageRequest request);
}