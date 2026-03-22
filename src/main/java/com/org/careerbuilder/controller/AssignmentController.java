package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.security.UserPrincipal;
import com.org.careerbuilder.service.AssignmentService;
import lombok.RequiredArgsConstructor;
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
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return service.getAssignments(user.getStudentId());
    }

    @PostMapping("/{id}/submit")
    public AssignmentSubmissionResponse submit(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String comments
    ) {
        return service.submit(id, user.getStudentId(), file, comments);
    }

    @GetMapping("/{id}/submission")
    public AssignmentSubmissionResponse view(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return service.getSubmission(id, user.getStudentId());
    }
}