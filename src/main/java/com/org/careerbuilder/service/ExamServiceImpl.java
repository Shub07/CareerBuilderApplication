package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamServiceInterface {

    private final ExamResultRepository examResultRepository;
    private final ExamResultBreakdownRepository breakdownRepository;
    private final StudentExamAnswerRepository answerRepository;
    private final ExamFeedbackRepository feedbackRepository;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    /**
     * 📌 Get Upcoming Exams
     */
    @Override
    public List<UpcomingExamResponse> getUpcomingExams(Long studentId) {
        log.info("Fetching upcoming exams for student: {}", studentId);
        
        List<ExamResult> upcomingResults = examResultRepository.findUpcomingExams(studentId);
        
        return upcomingResults.stream()
                .map(result -> mapToUpcomingExamResponse(result))
                .collect(Collectors.toList());
    }

    /**
     * 📌 Get Completed Exams
     */
    @Override
    public List<CompletedExamResponse> getCompletedExams(Long studentId) {
        log.info("Fetching completed exams for student: {}", studentId);
        
        List<ExamResult> completedResults = examResultRepository.findCompletedExams(studentId);
        
        return completedResults.stream()
                .map(result -> mapToCompletedExamResponse(result))
                .collect(Collectors.toList());
    }

    /**
     * 📌 Get Exam Result Detail (Full View with Breakdown)
     */
    @Override
    public ExamResultDetailResponse getExamResultDetail(Long examResultId, Long studentId) {
        log.info("Fetching exam result detail for examResult: {}, student: {}", examResultId, studentId);
        
        ExamResult examResult = examResultRepository.findById(examResultId)
                .orElseThrow(() -> new RuntimeException("Exam result not found: " + examResultId));
        
        // Verify student ownership
        if (!examResult.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("Unauthorized access to exam result");
        }
        
        // Calculate percentage
        int percentage = (examResult.getObtainedMarks() * 100) / examResult.getTotalMarks();
        String grade = calculateGrade(percentage);
        
        // Get marks breakdown
        List<ExamResultDetailResponse.MarksBreakdownSection> breakdown = 
                getMarksBreakdown(examResultId);
        
        // Get performance insights
        PerformanceInsightsResponse performanceInsights = 
                getPerformanceInsights(examResult);
        
        // Get teacher feedback
        TeacherFeedbackResponse feedback = 
                getTeacherFeedback(examResultId);
        
        // Get question-wise analysis
        List<QuestionWiseAnalysisResponse> questionAnalysis = 
                getQuestionWiseAnalysis(examResultId);
        
        return ExamResultDetailResponse.builder()
                .examId(examResult.getExam().getId())
                .subjectName(examResult.getSubject().getName())
                .examName(examResult.getExam().getName())
                .examDate(examResult.getExam().getExamDate().format(DATE_FORMAT))
                .totalScore(examResult.getObtainedMarks() + "/" + examResult.getTotalMarks())
                .percentage(percentage)
                .grade(grade)
                .marksBreakdown(breakdown)
                .performanceInsights(performanceInsights)
                .teacherFeedback(feedback)
                .questionWiseAnalysis(questionAnalysis)
                .build();
    }

    /**
     * Get Marks Breakdown
     */
    private List<ExamResultDetailResponse.MarksBreakdownSection> getMarksBreakdown(Long examResultId) {
        List<ExamResultBreakdown> breakdowns = breakdownRepository.findByExamResultId(examResultId);
        
        return breakdowns.stream()
                .map(bd -> ExamResultDetailResponse.MarksBreakdownSection.builder()
                        .sectionName(bd.getSectionName())
                        .obtainedMarks(bd.getObtainedMarks())
                        .totalMarks(bd.getTotalMarks())
                        .percentage(bd.getPercentage())
                        .visualPercentage(bd.getPercentage().intValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get Performance Insights
     */
    private PerformanceInsightsResponse getPerformanceInsights(ExamResult examResult) {
        Object[] stats = examResultRepository.getExamPerformanceStats(examResult.getExam().getId());
        
        Double classAverage = stats[0] != null ? ((Number) stats[0]).doubleValue() : 0.0;
        Double highestScore = stats[1] != null ? ((Number) stats[1]).doubleValue() : 0.0;
        
        // Calculate student score percentage
        double studentScore = (examResult.getObtainedMarks() * 100.0) / examResult.getTotalMarks();
        
        // Get rank
        Long rankCount = examResultRepository.getStudentRank(examResult.getId(), examResult.getExam().getId());
        Integer rank = (int) (rankCount + 1);
        
        return PerformanceInsightsResponse.builder()
                .classAverage(classAverage.intValue())
                .highestScore(highestScore.intValue())
                .yourRank(rank)
                .totalStudents(30)  // Replace with actual count if needed
                .rankText("#" + rank)
                .performanceLabel(getPerformanceLabel(studentScore))
                .comparisonWithAverage((int) (studentScore - classAverage))
                .isAboveAverage(studentScore >= classAverage)
                .build();
    }

    /**
     * Get Teacher Feedback
     */
    private TeacherFeedbackResponse getTeacherFeedback(Long examResultId) {
        Optional<ExamFeedback> feedback = feedbackRepository.findByExamResult_Id(examResultId);
        
        if (feedback.isPresent()) {
            ExamFeedback fb = feedback.get();
            return TeacherFeedbackResponse.builder()
                    .teacherName(fb.getTeacherName())
                    .teacherId(fb.getTeacherId())
                    .feedbackText(fb.getFeedbackText())
                    .feedbackDate(fb.getCreatedAt().format(DATE_FORMAT))
                    .isAvailable(true)
                    .build();
        }
        
        return TeacherFeedbackResponse.builder()
                .isAvailable(false)
                .build();
    }

    /**
     * Get Question-wise Analysis
     */
    private List<QuestionWiseAnalysisResponse> getQuestionWiseAnalysis(Long examResultId) {
        List<StudentExamAnswer> answers = answerRepository.findByExamResultId(examResultId);
        
        return answers.stream()
                .map(answer -> QuestionWiseAnalysisResponse.builder()
                        .questionNumber(answer.getQuestion().getQuestionNumber())
                        .questionText(answer.getQuestion().getQuestionText())
                        .status(answer.getStatus())
                        .statusLabel(formatStatus(answer.getStatus()))
                        .marksObtained(answer.getObtainedMarks())
                        .totalMarks(answer.getQuestion().getMarks())
                        .section(answer.getQuestion().getSection().getName())
                        .isCorrect("CORRECT".equals(answer.getStatus()))
                        .build())
                .collect(Collectors.toList());
    }

    // ============= HELPER METHODS =============

    private UpcomingExamResponse mapToUpcomingExamResponse(ExamResult result) {
        Exam exam = result.getExam();
        LocalDate examDate = exam.getExamDate();
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), examDate);
        
        String status;
        if (daysRemaining == 0) {
            status = "Today";
        } else if (daysRemaining == 1) {
            status = "Tomorrow";
        } else {
            status = "In " + daysRemaining + " days";
        }
        
        return UpcomingExamResponse.builder()
                .examId(exam.getId())
                .subjectName(result.getSubject().getName())
                .examName(exam.getName())
                .examDate(examDate)
                .startTime(exam.getStartTime())
                .durationHours(exam.getDurationMinutes() != null ? exam.getDurationMinutes() / 60 : 0)
                .durationText((exam.getDurationMinutes() != null ? exam.getDurationMinutes() / 60 : 0) + " hours")
                .topics("Laws of Motion, Energy")  // Get from database
                .examType("Written")  // Get from database
                .daysRemaining(daysRemaining)
                .isToday(daysRemaining == 0)
                .status(status)
                .build();
    }

    private CompletedExamResponse mapToCompletedExamResponse(ExamResult result) {
        int percentage = (result.getObtainedMarks() * 100) / result.getTotalMarks();
        String grade = calculateGrade(percentage);
        
        return CompletedExamResponse.builder()
                .examId(result.getExam().getId())
                .subjectName(result.getSubject().getName())
                .examName(result.getExam().getName())
                .score(result.getObtainedMarks() + "/" + result.getTotalMarks())
                .percentage(percentage)
                .grade(grade)
                .examDate(result.getExam().getExamDate().format(DATE_FORMAT))
                .subjectIcon("icon-" + result.getSubject().getName().toLowerCase())
                .build();
    }

    private String calculateGrade(int percentage) {
        if (percentage >= 90) return "Grade A";
        if (percentage >= 75) return "Grade B";
        if (percentage >= 60) return "Grade C";
        return "Grade D";
    }

    private String getPerformanceLabel(double score) {
        if (score >= 90) return "Excellent";
        if (score >= 75) return "Good";
        if (score >= 60) return "Average";
        return "Needs Improvement";
    }

    private String formatStatus(String status) {
        if (status == null) return "Pending";
        return status.charAt(0) + status.substring(1).toLowerCase();
    }
}

