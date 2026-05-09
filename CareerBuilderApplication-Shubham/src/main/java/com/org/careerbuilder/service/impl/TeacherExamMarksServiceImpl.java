package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.*;
import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.TeacherExamMarksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherExamMarksServiceImpl implements TeacherExamMarksService {

    private final ExamResultRepository examResultRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TeacherExamCardResponse> listExamsForMarks(Long facultyId, String className, String section, String examType, String status) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
        
        // Fetch exam results for the given criteria
        List<ExamResult> results = examResultRepository.findAll().stream()
                .filter(r -> r.getExam() != null && (className == null || r.getExam().getName().contains(className)))
                .filter(r -> section == null || (status != null))
                .collect(Collectors.toList());

        return results.stream().map(this::mapToExamCard).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ExamMarkEntryBoardResponse getMarkEntryBoard(Long facultyId, Long examId) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
        
        List<ExamResult> results = examResultRepository.findAll().stream()
                .filter(r -> r.getExam() != null && r.getExam().getId().equals(examId))
                .collect(Collectors.toList());

        if (results.isEmpty()) {
            throw new ResourceNotFoundException("No exam found with id: " + examId);
        }

        ExamResult first = results.get(0);
        return buildMarkEntryBoard(first, results);
    }

    @Override
    @Transactional
    public ExamMarkEntryBoardResponse saveMarks(Long facultyId, Long examId, ExamMarkEntryBatchRequest request) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
        
        for (ExamMarkEntryRequest markReq : request.getMarks()) {
            ExamResult result = examResultRepository.findById(markReq.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("ExamResult not found"));
            result.setObtainedMarks(markReq.getMarks().intValue());
            result.setRemarks(markReq.getRemark());
            examResultRepository.save(result);
        }

        return getMarkEntryBoard(facultyId, examId);
    }

    @Override
    @Transactional
    public ExamMarkEntryRowResponse updateMark(Long facultyId, Long examId, Long resultId, ExamMarkEntryRequest request) {
        ExamResult result = examResultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("ExamResult not found"));
        
        result.setObtainedMarks(request.getMarks().intValue());
        result.setRemarks(request.getRemark());
        ExamResult saved = examResultRepository.save(result);

        return mapToMarkEntryRow(saved);
    }

    @Override
    @Transactional
    public ExamMarkEntryBoardResponse submitAndLockMarks(Long facultyId, Long examId) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
        
        // Mark exam as submitted/locked
        List<ExamResult> results = examResultRepository.findAll().stream()
                .filter(r -> r.getExam() != null && r.getExam().getId().equals(examId))
                .collect(Collectors.toList());

        if (results.isEmpty()) {
            throw new ResourceNotFoundException("No exam found");
        }

        for (ExamResult result : results) {
            result.setMarksLocked(true);
            result.setMarksLockedAt(LocalDateTime.now());
            examResultRepository.save(result);
        }

        ExamResult first = results.get(0);
        return buildMarkEntryBoard(first, results);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamMarksHistoryResponse> getMarksHistory(Long facultyId, String className, String section) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
        
        List<ExamResult> results = examResultRepository.findAll().stream()
                .filter(r -> r.getExam() != null && r.isMarksLocked())
                .collect(Collectors.toList());

        return results.stream()
                .map(this::mapToMarksHistory)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ExamMarkEntryBoardResponse getMarksDetail(Long facultyId, Long examId) {
        return getMarkEntryBoard(facultyId, examId);
    }

    @Override
    @Transactional
    public ExamMarkEntryRowResponse editLockedMarks(Long facultyId, Long examId, ExamMarksEditRequest request) {
        ExamResult result = examResultRepository.findById(request.getResultId())
                .orElseThrow(() -> new ResourceNotFoundException("ExamResult not found"));
        
        if (!result.isMarksLocked()) {
            throw new IllegalStateException("Marks are not locked for this result");
        }

        result.setObtainedMarks(request.getMarks().intValue());
        result.setRemarks(request.getRemark());
        result.setMarksEditedAt(LocalDateTime.now());
        result.setMarksEditReason(request.getReasonForEdit());
        ExamResult saved = examResultRepository.save(result);

        return mapToMarkEntryRow(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ImportMarksPreviewResponse previewImportMarks(Long facultyId, Long examId, MultipartFile file) {
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            List<ImportMarksPreviewResponse.ImportedMarkRow> rows = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            int validCount = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null) continue;

                try {
                    Integer rollNo = (int) row.getCell(0).getNumericCellValue();
                    String markStr = row.getCell(1).getStringCellValue();
                    Double marks = Double.parseDouble(markStr);

                    ImportMarksPreviewResponse.ImportedMarkRow importRow = ImportMarksPreviewResponse.ImportedMarkRow.builder()
                            .lineNumber(i)
                            .rollNo(rollNo)
                            .marks(marks)
                            .isValid(true)
                            .build();
                    rows.add(importRow);
                    validCount++;
                } catch (Exception ex) {
                    ImportMarksPreviewResponse.ImportedMarkRow importRow = ImportMarksPreviewResponse.ImportedMarkRow.builder()
                            .lineNumber(i)
                            .isValid(false)
                            .errorMessage(ex.getMessage())
                            .build();
                    rows.add(importRow);
                    errors.add("Row " + i + ": " + ex.getMessage());
                }
            }

            return ImportMarksPreviewResponse.builder()
                    .totalRows(sheet.getLastRowNum())
                    .validRows(validCount)
                    .invalidRows(errors.size())
                    .rows(rows)
                    .errors(errors)
                    .build();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to parse file", ex);
        }
    }

    @Override
    @Transactional
    public ExamMarkEntryBoardResponse confirmImportMarks(Long facultyId, Long examId, List<ImportMarksPreviewResponse.ImportedMarkRow> rows) {
        for (ImportMarksPreviewResponse.ImportedMarkRow row : rows) {
            if (Boolean.TRUE.equals(row.getIsValid())) {
                // Match student by roll no and update marks
                List<ExamResult> results = examResultRepository.findAll().stream()
                        .filter(r -> r.getStudent() != null && r.getStudent().getRollNo().equals(row.getRollNo()))
                        .collect(Collectors.toList());
                
                if (!results.isEmpty()) {
                    ExamResult result = results.get(0);
                    result.setObtainedMarks(row.getMarks().intValue());
                    examResultRepository.save(result);
                }
            }
        }

        return getMarkEntryBoard(facultyId, examId);
    }

    @Override
    public byte[] exportMarks(Long facultyId, Long examId) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Marks");

            // Create header
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Roll No", "Student Name", "Marks", "Remark", "Status"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Add data
            List<ExamResult> results = examResultRepository.findAll().stream()
                    .filter(r -> r.getExam() != null && r.getExam().getId().equals(examId))
                    .collect(Collectors.toList());

            int rowNum = 1;
            for (ExamResult result : results) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(result.getStudent().getRollNo());
                row.createCell(1).setCellValue(result.getStudent().getFirstName() + " " + result.getStudent().getLastName());
                row.createCell(2).setCellValue(result.getObtainedMarks());
                row.createCell(3).setCellValue(result.getRemarks() != null ? result.getRemarks() : "");
                row.createCell(4).setCellValue(result.getObtainedMarks() >= 40 ? "PASS" : "FAIL");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();
            return out.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to export marks", ex);
        }
    }

    // Helper methods
    private TeacherExamCardResponse mapToExamCard(ExamResult result) {
        Exam exam = result.getExam();
        String status = result.isMarksLocked() ? "SUBMITTED" : "MARKS_PENDING";
        
        return TeacherExamCardResponse.builder()
                .id(exam.getId())
                .title(exam.getName())
                .className(exam.getName())
                .status(status)
                .totalMarks(exam.getTotalMarks())
                .build();
    }

    private ExamMarkEntryBoardResponse buildMarkEntryBoard(ExamResult first, List<ExamResult> results) {
        Exam exam = first.getExam();
        List<ExamMarkEntryRowResponse> rows = results.stream()
                .map(this::mapToMarkEntryRow)
                .collect(Collectors.toList());

        return ExamMarkEntryBoardResponse.builder()
                .examId(exam.getId())
                .examTitle(exam.getName())
                .totalMarks(exam.getTotalMarks())
                .totalStudents(results.size())
                .presentCount((int) results.stream().filter(r -> r.getObtainedMarks() != null).count())
                .students(rows)
                .isLocked(first.isMarksLocked())
                .build();
    }

    private ExamMarkEntryRowResponse mapToMarkEntryRow(ExamResult result) {
        Student student = result.getStudent();
        String status = (result.getObtainedMarks() != null && result.getObtainedMarks() >= 40) ? "PASS" : "FAIL";

        return ExamMarkEntryRowResponse.builder()
                .studentId(student.getId())
                .rollNo(student.getRollNo())
                .studentName(student.getFirstName() + " " + student.getLastName())
                .marks(result.getObtainedMarks() != null ? (double) result.getObtainedMarks() : null)
                .remark(result.getRemarks())
                .status(status)
                .build();
    }

    private ExamMarksHistoryResponse mapToMarksHistory(ExamResult result) {
        Exam exam = result.getExam();
        return ExamMarksHistoryResponse.builder()
                .examId(exam.getId())
                .examTitle(exam.getName())
                .submittedOn(result.getMarksLockedAt())
                .marksStatus("LOCKED")
                .build();
    }
}

