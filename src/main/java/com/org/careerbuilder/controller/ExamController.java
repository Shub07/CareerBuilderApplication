package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.security.UserPrincipal;
import com.org.careerbuilder.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    /**
     * 📌 Completed Exams Tab
     */
    @GetMapping("/completed")
    public List<CompletedExamResponse> completed(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return examService.getCompleted(user.getStudentId());
    }

    /**
     * 📌 View Result Button
     */
    @GetMapping("/result/{examId}")
    public ExamDetailResponse result(
            @PathVariable Long examId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return examService.getResult(examId, user.getStudentId());
    }
}