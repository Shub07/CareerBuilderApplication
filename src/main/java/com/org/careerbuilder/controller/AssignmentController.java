package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.security.UserPrincipal;
import com.org.careerbuilder.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/student/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService service;

    @GetMapping
    public List<AssignmentCardResponse> list(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Long studentId
    ) {
        Long resolvedStudentId = resolveStudentId(user, studentId);
        return service.getAssignments(resolvedStudentId);
    }

    @PostMapping("/{id}/submit")
    public AssignmentSubmissionResponse submit(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Long studentId,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String comments,
            @RequestParam(defaultValue = "false") boolean allowLate
    ) {
        Long resolvedStudentId = resolveStudentId(user, studentId);
        return service.submit(id, resolvedStudentId, file, comments, allowLate);
    }

    @GetMapping("/{id}/submission")
    public AssignmentSubmissionResponse view(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Long studentId
    ) {
        Long resolvedStudentId = resolveStudentId(user, studentId);
        return service.getSubmission(id, resolvedStudentId);
    }

    @GetMapping("/{id}/submission/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Long studentId
    ) {
        Long resolvedStudentId = resolveStudentId(user, studentId);
        Resource resource = service.downloadSubmissionFile(id, resolvedStudentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=assignment_submission_" + id)
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