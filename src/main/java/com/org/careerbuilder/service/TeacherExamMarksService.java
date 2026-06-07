package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.ExamMarkEntryBatchRequest;
import com.org.careerbuilder.dto.request.ExamMarkEntryRequest;
import com.org.careerbuilder.dto.request.ExamMarksEditRequest;
import com.org.careerbuilder.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TeacherExamMarksService {

    TeacherExamFiltersResponse getExamFilters(Long facultyId);

    List<TeacherExamCardResponse> listExamsForMarks(
            Long facultyId,
            Boolean history,
            String className,
            String section,
            String examType,
            String status,
            Long subjectId);

    ExamMarkEntryBoardResponse getMarkEntryBoard(Long facultyId, Long examId);

    ExamMarkEntryBoardResponse saveMarks(Long facultyId, Long examId, ExamMarkEntryBatchRequest request);

    ExamMarkEntryRowResponse updateMark(Long facultyId, Long examId, Long resultId, ExamMarkEntryRequest request);

    ExamMarkEntryBoardResponse submitAndLockMarks(Long facultyId, Long examId);

    List<ExamMarksHistoryResponse> getMarksHistory(Long facultyId, String className, String section);

    ExamMarkEntryBoardResponse getMarksDetail(Long facultyId, Long examId);

    ExamMarkEntryRowResponse editLockedMarks(Long facultyId, Long examId, ExamMarksEditRequest request);

    ImportMarksPreviewResponse previewImportMarks(Long facultyId, Long examId, MultipartFile file);

    ExamMarkEntryBoardResponse confirmImportMarks(Long facultyId, Long examId, List<ImportMarksPreviewResponse.ImportedMarkRow> rows);

    byte[] exportMarks(Long facultyId, Long examId);
}
