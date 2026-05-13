package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.response.LessonPlanDtos;
import com.org.careerbuilder.security.UserPrincipal;
import com.org.careerbuilder.service.StudentLessonPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/lesson-plans")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.OPTIONS})
public class StudentLessonPlanController {

    private final StudentLessonPlanService studentLessonPlanService;

    @GetMapping("/topics")
    public ResponseEntity<List<LessonPlanDtos.StudentTopicRow>> listTopics(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Long studentId,
            @RequestParam Long subjectId) {
        return ResponseEntity.ok(studentLessonPlanService.listTopics(resolveStudentId(user, studentId), subjectId));
    }

    @GetMapping("/topics/{topicId}")
    public ResponseEntity<LessonPlanDtos.TopicDetailResponse> topicDetail(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Long studentId,
            @PathVariable Long topicId) {
        return ResponseEntity.ok(studentLessonPlanService.getStudentTopicDetail(resolveStudentId(user, studentId), topicId));
    }

    @GetMapping("/topics/{topicId}/session-logs")
    public ResponseEntity<List<LessonPlanDtos.SessionLogRow>> sessionLogs(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Long studentId,
            @PathVariable Long topicId) {
        return ResponseEntity.ok(studentLessonPlanService.listStudentSessionLogs(resolveStudentId(user, studentId), topicId));
    }

    @GetMapping("/learning-materials")
    public ResponseEntity<List<LessonPlanDtos.StudentMaterialRow>> materials(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(studentLessonPlanService.listMaterials(
                resolveStudentId(user, studentId), subjectId, topicId, search));
    }

    @GetMapping("/learning-materials/{materialId}/download")
    public ResponseEntity<Resource> downloadMaterial(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Long studentId,
            @PathVariable Long materialId) {
        Resource resource = studentLessonPlanService.downloadMaterial(resolveStudentId(user, studentId), materialId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .body(resource);
    }

    private Long resolveStudentId(UserPrincipal user, Long studentId) {
        if (studentId != null) {
            return studentId;
        }
        if (user != null && user.getStudentId() != null) {
            return user.getStudentId();
        }
        throw new IllegalArgumentException("studentId is required");
    }
}
