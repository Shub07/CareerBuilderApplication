package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.*;
import com.org.careerbuilder.dto.response.TeacherAssignmentDtos;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface TeacherAssignmentService {

    TeacherAssignmentDtos.AssignmentFiltersResponse getFilters(Long facultyId);

    TeacherAssignmentDtos.AssignmentListPage listAssignments(
            Long facultyId,
            String view,
            String className,
            String section,
            Long subjectId,
            LocalDate dueFrom,
            LocalDate dueTo,
            Integer month,
            String search,
            String displayStatus,
            Pageable pageable);

    TeacherAssignmentDtos.AssignmentDetailResponse getDetail(Long facultyId, Long assignmentId);

    TeacherAssignmentDtos.SubmissionsPage listSubmissions(
            Long facultyId, Long assignmentId, String statusTab, Pageable pageable);

    TeacherAssignmentDtos.SubmissionReviewResponse getSubmissionReview(Long facultyId, Long assignmentId, Long submissionId);

    Long createAssignment(Long facultyId, TeacherAssignmentCreateRequest request, MultipartFile attachment);

    void updateAssignment(Long facultyId, Long assignmentId, TeacherAssignmentUpdateRequest request, MultipartFile attachment);

    void deleteAssignment(Long facultyId, Long assignmentId);

    TeacherAssignmentDtos.DuplicateAssignmentResponse duplicateAssignment(Long facultyId, Long assignmentId);

    void gradeSubmission(Long facultyId, Long assignmentId, Long submissionId, TeacherGradeSubmissionRequest request);

    void returnSubmission(Long facultyId, Long assignmentId, Long submissionId, TeacherReturnSubmissionRequest request);

    Resource downloadSubmissionFile(Long facultyId, Long assignmentId, Long studentId);

    Resource exportAllSubmissionsZip(Long facultyId, Long assignmentId);
}
