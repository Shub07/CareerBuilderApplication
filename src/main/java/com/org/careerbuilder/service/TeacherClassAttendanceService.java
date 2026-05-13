package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.ClassAttendanceSaveDraftRequest;
import com.org.careerbuilder.dto.request.ClassAttendanceSubmitRequest;
import com.org.careerbuilder.dto.response.TeacherClassAttendanceDtos;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface TeacherClassAttendanceService {

    TeacherClassAttendanceDtos.FiltersResponse getFilters(Long facultyId);

    TeacherClassAttendanceDtos.RosterResponse getRoster(
            Long facultyId, String className, String section, Long subjectId, LocalDate sessionDate);

    TeacherClassAttendanceDtos.RosterResponse saveDraft(Long facultyId, ClassAttendanceSaveDraftRequest request);

    TeacherClassAttendanceDtos.SubmitResponse submit(
            Long facultyId, ClassAttendanceSubmitRequest request, MultipartFile proof);

    TeacherClassAttendanceDtos.RosterResponse markAllPresent(
            Long facultyId, String className, String section, Long subjectId, LocalDate sessionDate);

    void resetMarks(Long facultyId, String className, String section, Long subjectId, LocalDate sessionDate);

    TeacherClassAttendanceDtos.ImportPreviewResponse importPreview(
            Long facultyId, String className, String section, Long subjectId, LocalDate sessionDate, MultipartFile file);

    TeacherClassAttendanceDtos.HistoryPage getHistory(
            Long facultyId,
            LocalDate from,
            LocalDate to,
            String className,
            String section,
            Long subjectId,
            Pageable pageable);

    Resource exportHistoryCsv(
            Long facultyId,
            LocalDate from,
            LocalDate to,
            String className,
            String section,
            Long subjectId);

    TeacherClassAttendanceDtos.SessionDetailResponse getSessionDetail(Long facultyId, Long sessionId);
}
