package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.*;
import com.org.careerbuilder.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TeacherExamMarksService {
    // List exams for marks entry
    List<TeacherExamCardResponse> listExamsForMarks(Long facultyId, String className, String section, String examType, String status);

    // Get marks entry board
    ExamMarkEntryBoardResponse getMarkEntryBoard(Long facultyId, Long examId);

    // Save marks (draft)
    ExamMarkEntryBoardResponse saveMarks(Long facultyId, Long examId, ExamMarkEntryBatchRequest request);

    // Update individual mark
    ExamMarkEntryRowResponse updateMark(Long facultyId, Long examId, Long resultId, ExamMarkEntryRequest request);

    // Submit and lock marks
    ExamMarkEntryBoardResponse submitAndLockMarks(Long facultyId, Long examId);

    // Get marks history
    List<ExamMarksHistoryResponse> getMarksHistory(Long facultyId, String className, String section);

    // View locked marks
    ExamMarkEntryBoardResponse getMarksDetail(Long facultyId, Long examId);

    // Edit locked marks (with reason)
    ExamMarkEntryRowResponse editLockedMarks(Long facultyId, Long examId, ExamMarksEditRequest request);

    // Import marks from sheet (precheck)
    ImportMarksPreviewResponse previewImportMarks(Long facultyId, Long examId, MultipartFile file);

    // Confirm import
    ExamMarkEntryBoardResponse confirmImportMarks(Long facultyId, Long examId, List<ImportMarksPreviewResponse.ImportedMarkRow> rows);

    // Export marks as CSV/Excel
    byte[] exportMarks(Long facultyId, Long examId);
}

