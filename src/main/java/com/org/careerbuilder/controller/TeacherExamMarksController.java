package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.ExamMarkEntryBatchRequest;
import com.org.careerbuilder.dto.request.ExamMarkEntryRequest;
import com.org.careerbuilder.dto.request.ExamMarksEditRequest;
import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.service.TeacherExamMarksService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/exams")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class TeacherExamMarksController {

    private final TeacherExamMarksService examMarksService;

    /**
     * Dropdown data for class, subject, exam type, and status filters on the exams UI.
     */
    @GetMapping("/{facultyId}/filters")
    public ResponseEntity<TeacherExamFiltersResponse> getExamFilters(@PathVariable Long facultyId) {
        return ResponseEntity.ok(examMarksService.getExamFilters(facultyId));
    }

    /**
     * List exams as cards for marks entry (active) or submitted history cards when {@code history=true}.
     */
    @GetMapping("/{facultyId}/marks-entry")
    public ResponseEntity<List<TeacherExamCardResponse>> listExamsForMarksEntry(
            @PathVariable Long facultyId,
            @RequestParam(required = false) Boolean history,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String examType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long subjectId) {
        List<TeacherExamCardResponse> exams = examMarksService.listExamsForMarks(
                facultyId, history, className, section, examType, status, subjectId);
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/{facultyId}/marks-entry/{examId}")
    public ResponseEntity<ExamMarkEntryBoardResponse> getMarkEntryBoard(
            @PathVariable Long facultyId,
            @PathVariable Long examId) {
        ExamMarkEntryBoardResponse board = examMarksService.getMarkEntryBoard(facultyId, examId);
        return ResponseEntity.ok(board);
    }

    @PostMapping("/{facultyId}/marks-entry/{examId}/save")
    public ResponseEntity<ExamMarkEntryBoardResponse> saveMarks(
            @PathVariable Long facultyId,
            @PathVariable Long examId,
            @Valid @RequestBody ExamMarkEntryBatchRequest request) {
        ExamMarkEntryBoardResponse board = examMarksService.saveMarks(facultyId, examId, request);
        return ResponseEntity.ok(board);
    }

    @PatchMapping("/{facultyId}/marks-entry/{examId}/{resultId}")
    public ResponseEntity<ExamMarkEntryRowResponse> updateMark(
            @PathVariable Long facultyId,
            @PathVariable Long examId,
            @PathVariable Long resultId,
            @Valid @RequestBody ExamMarkEntryRequest request) {
        ExamMarkEntryRowResponse row = examMarksService.updateMark(facultyId, examId, resultId, request);
        return ResponseEntity.ok(row);
    }

    @PostMapping("/{facultyId}/marks-entry/{examId}/submit")
    public ResponseEntity<ExamMarkEntryBoardResponse> submitAndLockMarks(
            @PathVariable Long facultyId,
            @PathVariable Long examId) {
        ExamMarkEntryBoardResponse board = examMarksService.submitAndLockMarks(facultyId, examId);
        return ResponseEntity.ok(board);
    }

    @GetMapping("/{facultyId}/marks-history")
    public ResponseEntity<List<ExamMarksHistoryResponse>> getMarksHistory(
            @PathVariable Long facultyId,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section) {
        List<ExamMarksHistoryResponse> history = examMarksService.getMarksHistory(facultyId, className, section);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{facultyId}/marks-detail/{examId}")
    public ResponseEntity<ExamMarkEntryBoardResponse> getMarksDetail(
            @PathVariable Long facultyId,
            @PathVariable Long examId) {
        ExamMarkEntryBoardResponse detail = examMarksService.getMarksDetail(facultyId, examId);
        return ResponseEntity.ok(detail);
    }

    @PutMapping("/{facultyId}/marks-detail/{examId}/edit")
    public ResponseEntity<ExamMarkEntryRowResponse> editLockedMarks(
            @PathVariable Long facultyId,
            @PathVariable Long examId,
            @Valid @RequestBody ExamMarksEditRequest request) {
        ExamMarkEntryRowResponse row = examMarksService.editLockedMarks(facultyId, examId, request);
        return ResponseEntity.ok(row);
    }

    @PostMapping("/{facultyId}/marks-entry/{examId}/import-preview")
    public ResponseEntity<ImportMarksPreviewResponse> previewImportMarks(
            @PathVariable Long facultyId,
            @PathVariable Long examId,
            @RequestParam MultipartFile file) {
        ImportMarksPreviewResponse preview = examMarksService.previewImportMarks(facultyId, examId, file);
        return ResponseEntity.ok(preview);
    }

    @PostMapping("/{facultyId}/marks-entry/{examId}/import-confirm")
    public ResponseEntity<ExamMarkEntryBoardResponse> confirmImportMarks(
            @PathVariable Long facultyId,
            @PathVariable Long examId,
            @RequestBody List<ImportMarksPreviewResponse.ImportedMarkRow> rows) {
        ExamMarkEntryBoardResponse board = examMarksService.confirmImportMarks(facultyId, examId, rows);
        return ResponseEntity.ok(board);
    }

    @GetMapping("/{facultyId}/marks-detail/{examId}/export")
    public ResponseEntity<byte[]> exportMarks(
            @PathVariable Long facultyId,
            @PathVariable Long examId) {
        byte[] content = examMarksService.exportMarks(facultyId, examId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("exam_marks_" + examId + ".xlsx")
                .build());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }
}
