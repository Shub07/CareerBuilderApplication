package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.TeacherStudentRemarkRequest;
import com.org.careerbuilder.dto.response.TeacherStudentPerformanceDtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface TeacherStudentPerformanceService {

    TeacherStudentPerformanceDtos.FiltersResponse getFilters(Long facultyId, String className, String section);

    TeacherStudentPerformanceDtos.KpiResponse getClassKpis(Long facultyId, String className, String section);

    List<TeacherStudentPerformanceDtos.StudentRowResponse> getStudentRows(
            Long facultyId,
            String className,
            String section,
            Long subjectId,
            Long examId);

    TeacherStudentPerformanceDtos.StudentDetailResponse getStudentDetail(
            Long facultyId, Long studentId, Long subjectId, Long examId);

    TeacherStudentPerformanceDtos.OverviewResponse getStudentOverview(Long facultyId, Long studentId);

    List<TeacherStudentPerformanceDtos.ExamPerformanceRow> getStudentExams(Long facultyId, Long studentId);

    List<TeacherStudentPerformanceDtos.QuizPerformanceRow> getStudentQuizzes(Long facultyId, Long studentId);

    List<TeacherStudentPerformanceDtos.AssignmentPerformanceRow> getStudentAssignments(Long facultyId, Long studentId);

    TeacherStudentPerformanceDtos.AttendanceSummary getStudentAttendanceSummary(
            Long facultyId, Long studentId, LocalDate from, LocalDate to);

    Page<TeacherStudentPerformanceDtos.AttendanceDetailRow> getStudentAttendanceDetails(
            Long facultyId,
            Long studentId,
            LocalDate from,
            LocalDate to,
            Pageable pageable);

    void saveRemark(Long facultyId, Long studentId, TeacherStudentRemarkRequest request);

    TeacherStudentPerformanceDtos.EmailQueuedResponse queueEmailToParents(Long facultyId, Long studentId);

    byte[] buildPerformancePdf(Long facultyId, Long studentId);
}
