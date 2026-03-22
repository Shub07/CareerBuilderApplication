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

    List<TeacherCardResponse> getTeachers(Long studentId, Long subjectId);

    void sendTeacherMessage(Long studentId, Long teacherId, TeacherMessageRequest request);
}