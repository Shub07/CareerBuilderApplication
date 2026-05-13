package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.*;
import com.org.careerbuilder.dto.response.TeacherMyClassesDtos;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface TeacherMyClassesService {

    TeacherMyClassesDtos.ClassSessionListResponse listSessions(
            Long facultyId, String timeRange, String academicYear, String search);

    TeacherMyClassesDtos.ExtraSessionCreatedResponse createExtraSession(Long facultyId, TeacherExtraSessionRequest request);

    TeacherMyClassesDtos.SessionHeaderResponse getSessionHeader(
            Long facultyId, String sessionKind, Long refId, LocalDate sessionDate);

    TeacherMyClassesDtos.LessonNoteResponse getLessonNotes(
            Long facultyId, String sessionKind, Long refId, LocalDate sessionDate);

    TeacherMyClassesDtos.LessonNoteResponse saveLessonNotes(
            Long facultyId, TeacherLessonNoteRequest request);

    TeacherMyClassesDtos.AttendanceRosterResponse getAttendanceRoster(
            Long facultyId, String sessionKind, Long refId, LocalDate sessionDate);

    void submitSessionAttendance(Long facultyId, TeacherSessionAttendanceSubmitRequest request);

    TeacherMyClassesDtos.ImportAttendancePreviewResponse previewAttendanceImport(
            Long facultyId, String sessionKind, Long refId, LocalDate sessionDate, MultipartFile file);

    TeacherMyClassesDtos.AssignmentPublishedResponse publishSessionAssignment(
            Long facultyId,
            String sessionKind,
            Long refId,
            LocalDate sessionDate,
            String title,
            String description,
            LocalDate dueDate,
            MultipartFile attachment);

    Resource exportTeachingLog(Long facultyId, String academicYear);

    TeacherMyClassesDtos.AcademicYearsResponse listAcademicYears();

    TeacherMyClassesDtos.TeacherPortalSearchResponse search(Long facultyId, String q);
}
