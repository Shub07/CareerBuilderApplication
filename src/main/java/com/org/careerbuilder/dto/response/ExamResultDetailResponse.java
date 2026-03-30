package com.org.careerbuilder.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * 📌 Exam Result Detail - Complete Response
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class ExamResultDetailResponse {
    
    @JsonProperty("exam_id")
    private Long examId;
    
    @JsonProperty("subject_name")
    private String subjectName;
    
    @JsonProperty("exam_name")
    private String examName;
    
    @JsonProperty("exam_date")
    private String examDate;  // "Nov 5, 2025"
    
    @JsonProperty("total_score")
    private String totalScore;  // "97/100"
    
    @JsonProperty("percentage")
    private Integer percentage;
    
    @JsonProperty("grade")
    private String grade;  // "Grade A"
    
    // ============= MARKS BREAKDOWN =============
    @JsonProperty("marks_breakdown")
    private List<MarksBreakdownSection> marksBreakdown;
    
    // ============= PERFORMANCE INSIGHTS =============
    @JsonProperty("performance_insights")
    private PerformanceInsightsResponse performanceInsights;
    
    // ============= TEACHER FEEDBACK =============
    @JsonProperty("teacher_feedback")
    private TeacherFeedbackResponse teacherFeedback;
    
    // ============= QUESTION-WISE ANALYSIS =============
    @JsonProperty("question_wise_analysis")
    private List<QuestionWiseAnalysisResponse> questionWiseAnalysis;
    
    /**
     * Marks Breakdown by Section
     */
    @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
    public static class MarksBreakdownSection {
        @JsonProperty("section_name")
        private String sectionName;  // "Section A: MCQ"
        
        @JsonProperty("obtained_marks")
        private Integer obtainedMarks;
        
        @JsonProperty("total_marks")
        private Integer totalMarks;
        
        @JsonProperty("percentage")
        private Double percentage;
        
        @JsonProperty("visual_percentage")
        private Integer visualPercentage;  // For progress bar
    }
}

