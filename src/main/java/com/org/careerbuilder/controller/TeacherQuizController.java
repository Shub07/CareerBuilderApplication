package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.TeacherQuizReviewSaveRequest;
import com.org.careerbuilder.dto.request.TeacherQuizUpsertRequest;
import com.org.careerbuilder.dto.response.TeacherQuizDtos;
import com.org.careerbuilder.service.TeacherQuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/quizzes")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class TeacherQuizController {

    private final TeacherQuizService teacherQuizService;

    @GetMapping("/{facultyId}/filters")
    public ResponseEntity<TeacherQuizDtos.QuizFiltersResponse> filters(@PathVariable Long facultyId) {
        return ResponseEntity.ok(teacherQuizService.getFilters(facultyId));
    }

    @GetMapping("/{facultyId}")
    public ResponseEntity<TeacherQuizDtos.QuizListPage> list(
            @PathVariable Long facultyId,
            @RequestParam(defaultValue = "active") String view,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String lifecycleStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        return ResponseEntity.ok(teacherQuizService.listQuizzes(
                facultyId, view, className, section, subjectId, lifecycleStatus, dateFrom, dateTo, search, pageable));
    }

    @GetMapping("/{facultyId}/{quizId}")
    public ResponseEntity<TeacherQuizDtos.QuizDetailResponse> detail(
            @PathVariable Long facultyId,
            @PathVariable Long quizId) {
        return ResponseEntity.ok(teacherQuizService.getQuizDetail(facultyId, quizId));
    }

    @PostMapping("/{facultyId}")
    public ResponseEntity<Map<String, Long>> create(
            @PathVariable Long facultyId,
            @Valid @RequestBody TeacherQuizUpsertRequest request) {
        Long id = teacherQuizService.createQuiz(facultyId, request);
        return ResponseEntity.status(201).body(Map.of("quizId", id));
    }

    @PutMapping("/{facultyId}/{quizId}")
    public ResponseEntity<Void> update(
            @PathVariable Long facultyId,
            @PathVariable Long quizId,
            @Valid @RequestBody TeacherQuizUpsertRequest request) {
        teacherQuizService.updateQuiz(facultyId, quizId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{facultyId}/{quizId}")
    public ResponseEntity<Void> delete(@PathVariable Long facultyId, @PathVariable Long quizId) {
        teacherQuizService.deleteQuiz(facultyId, quizId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{facultyId}/{quizId}/duplicate")
    public ResponseEntity<TeacherQuizDtos.DuplicateQuizResponse> duplicate(
            @PathVariable Long facultyId,
            @PathVariable Long quizId) {
        return ResponseEntity.ok(teacherQuizService.duplicateQuiz(facultyId, quizId));
    }

    @PatchMapping("/{facultyId}/{quizId}/publish")
    public ResponseEntity<Void> publish(@PathVariable Long facultyId, @PathVariable Long quizId) {
        teacherQuizService.publishQuiz(facultyId, quizId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{facultyId}/{quizId}/conduct")
    public ResponseEntity<Void> conduct(@PathVariable Long facultyId, @PathVariable Long quizId) {
        teacherQuizService.markConducted(facultyId, quizId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{facultyId}/{quizId}/results")
    public ResponseEntity<TeacherQuizDtos.QuizResultsPage> results(
            @PathVariable Long facultyId,
            @PathVariable Long quizId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(200, Math.max(1, size)));
        return ResponseEntity.ok(teacherQuizService.getResults(facultyId, quizId, search, pageable));
    }

    @GetMapping("/{facultyId}/{quizId}/submissions/{submissionId}/review")
    public ResponseEntity<TeacherQuizDtos.QuizSubmissionReviewResponse> review(
            @PathVariable Long facultyId,
            @PathVariable Long quizId,
            @PathVariable Long submissionId) {
        return ResponseEntity.ok(teacherQuizService.getSubmissionReview(facultyId, quizId, submissionId));
    }

    @PutMapping("/{facultyId}/{quizId}/submissions/{submissionId}/review")
    public ResponseEntity<Void> saveReview(
            @PathVariable Long facultyId,
            @PathVariable Long quizId,
            @PathVariable Long submissionId,
            @Valid @RequestBody TeacherQuizReviewSaveRequest request) {
        teacherQuizService.saveSubmissionReview(facultyId, quizId, submissionId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{facultyId}/{quizId}/export-results.csv")
    public ResponseEntity<Resource> exportCsv(@PathVariable Long facultyId, @PathVariable Long quizId) {
        Resource resource = teacherQuizService.exportResultsCsv(facultyId, quizId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"quiz-results.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(resource);
    }
}
