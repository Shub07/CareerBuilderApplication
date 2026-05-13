package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.*;
import com.org.careerbuilder.dto.response.LessonPlanDtos;
import com.org.careerbuilder.service.TeacherLessonPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/lesson-plans")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class TeacherLessonPlanController {

    private final TeacherLessonPlanService teacherLessonPlanService;

    @GetMapping("/{facultyId}/filters")
    public ResponseEntity<LessonPlanDtos.LessonPlanFiltersResponse> filters(@PathVariable Long facultyId) {
        return ResponseEntity.ok(teacherLessonPlanService.getFilters(facultyId));
    }

    @GetMapping("/{facultyId}/topics")
    public ResponseEntity<List<LessonPlanDtos.TopicRow>> listTopics(
            @PathVariable Long facultyId,
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam Long subjectId) {
        return ResponseEntity.ok(teacherLessonPlanService.listTopics(facultyId, className, section, subjectId));
    }

    @GetMapping("/{facultyId}/topics/{topicId}")
    public ResponseEntity<LessonPlanDtos.TopicDetailResponse> getTopic(
            @PathVariable Long facultyId,
            @PathVariable Long topicId) {
        return ResponseEntity.ok(teacherLessonPlanService.getTopicDetail(facultyId, topicId));
    }

    @PostMapping("/{facultyId}/topics")
    public ResponseEntity<Map<String, Long>> createTopic(
            @PathVariable Long facultyId,
            @Valid @RequestBody LessonPlanTopicUpsertRequest request) {
        Long id = teacherLessonPlanService.createTopic(facultyId, request);
        return ResponseEntity.status(201).body(Map.of("topicId", id));
    }

    @PutMapping("/{facultyId}/topics/{topicId}")
    public ResponseEntity<Void> updateTopic(
            @PathVariable Long facultyId,
            @PathVariable Long topicId,
            @Valid @RequestBody LessonPlanTopicUpsertRequest request) {
        teacherLessonPlanService.updateTopic(facultyId, topicId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{facultyId}/topics/{topicId}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long facultyId, @PathVariable Long topicId) {
        teacherLessonPlanService.deleteTopic(facultyId, topicId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{facultyId}/topics/{topicId}/session-logs")
    public ResponseEntity<List<LessonPlanDtos.SessionLogRow>> listLogs(
            @PathVariable Long facultyId,
            @PathVariable Long topicId) {
        return ResponseEntity.ok(teacherLessonPlanService.listSessionLogs(facultyId, topicId));
    }

    @PostMapping(value = "/{facultyId}/topics/{topicId}/session-logs", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Long>> addLogJson(
            @PathVariable Long facultyId,
            @PathVariable Long topicId,
            @Valid @RequestBody LessonPlanSessionLogRequest request) {
        Long id = teacherLessonPlanService.addSessionLog(facultyId, topicId, request, null);
        return ResponseEntity.status(201).body(Map.of("logId", id));
    }

    @PostMapping(value = "/{facultyId}/topics/{topicId}/session-logs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Long>> addLogMultipart(
            @PathVariable Long facultyId,
            @PathVariable Long topicId,
            @Valid @RequestPart("data") LessonPlanSessionLogRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        Long id = teacherLessonPlanService.addSessionLog(facultyId, topicId, request, file);
        return ResponseEntity.status(201).body(Map.of("logId", id));
    }

    @PutMapping(value = "/{facultyId}/topics/{topicId}/session-logs/{logId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateLogJson(
            @PathVariable Long facultyId,
            @PathVariable Long topicId,
            @PathVariable Long logId,
            @Valid @RequestBody LessonPlanSessionLogRequest request) {
        teacherLessonPlanService.updateSessionLog(facultyId, topicId, logId, request, null);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{facultyId}/topics/{topicId}/session-logs/{logId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateLogMultipart(
            @PathVariable Long facultyId,
            @PathVariable Long topicId,
            @PathVariable Long logId,
            @Valid @RequestPart("data") LessonPlanSessionLogRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        teacherLessonPlanService.updateSessionLog(facultyId, topicId, logId, request, file);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{facultyId}/topics/{topicId}/session-logs/{logId}")
    public ResponseEntity<Void> deleteLog(
            @PathVariable Long facultyId,
            @PathVariable Long topicId,
            @PathVariable Long logId) {
        teacherLessonPlanService.deleteSessionLog(facultyId, topicId, logId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{facultyId}/materials")
    public ResponseEntity<List<LessonPlanDtos.LearningMaterialRow>> listMaterials(
            @PathVariable Long facultyId,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(teacherLessonPlanService.listMaterials(facultyId, className, section, subjectId, topicId, search));
    }

    @PostMapping(value = "/{facultyId}/materials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Long>> uploadMaterial(
            @PathVariable Long facultyId,
            @Valid @RequestPart("data") LearningMaterialMetadataRequest meta,
            @RequestPart("file") MultipartFile file) {
        Long id = teacherLessonPlanService.uploadMaterial(facultyId, meta, file);
        return ResponseEntity.status(201).body(Map.of("materialId", id));
    }

    @DeleteMapping("/{facultyId}/materials/{materialId}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long facultyId, @PathVariable Long materialId) {
        teacherLessonPlanService.deleteMaterial(facultyId, materialId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{facultyId}/materials/{materialId}/visibility")
    public ResponseEntity<Void> visibility(
            @PathVariable Long facultyId,
            @PathVariable Long materialId,
            @Valid @RequestBody LearningMaterialVisibilityRequest request) {
        teacherLessonPlanService.setMaterialVisibility(facultyId, materialId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{facultyId}/materials/{materialId}/download")
    public ResponseEntity<Resource> downloadMaterial(
            @PathVariable Long facultyId,
            @PathVariable Long materialId) {
        Resource resource = teacherLessonPlanService.downloadMaterialTeacher(facultyId, materialId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .body(resource);
    }
}
