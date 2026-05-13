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

/**
 * Alias routes for the learning-material repository (same service as {@link StudentLessonPlanController}).
 */
@RestController
@RequestMapping("/api/student/learning-materials")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.OPTIONS})
public class StudentLearningMaterialController {

    private final StudentLessonPlanService studentLessonPlanService;

    @GetMapping
    public ResponseEntity<List<LessonPlanDtos.StudentMaterialRow>> list(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(studentLessonPlanService.listMaterials(
                resolveStudentId(user, studentId), subjectId, topicId, search));
    }

    @GetMapping("/{materialId}/download")
    public ResponseEntity<Resource> download(
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
