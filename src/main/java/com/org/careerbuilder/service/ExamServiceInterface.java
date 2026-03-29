package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.*;

import java.util.List;

/**
 * 📌 Exam Service Interface
 */
public interface ExamServiceInterface {
    
    /**
     * Get upcoming exams for a student
     */
    List<UpcomingExamResponse> getUpcomingExams(Long studentId);
    
    /**
     * Get completed exams for a student
     */
    List<CompletedExamResponse> getCompletedExams(Long studentId);
    
    /**
     * Get detailed exam result with breakdown, feedback, analysis
     */
    ExamResultDetailResponse getExamResultDetail(Long examResultId, Long studentId);
}

