package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamResultRepository examResultRepository;
    private final ExamResultBreakdownRepository breakdownRepository;
    private final StudentExamAnswerRepository answerRepository;
    private final ExamFeedbackRepository feedbackRepository;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    /**
     * 📌 Get Upcoming Exams
     */
    public List<UpcomingExamResponse> getUpcomingExams(Long studentId) {
        log.info("Fetching upcoming exams for student: {}", studentId);
        
        try {
            // Verify student exists
            List<ExamResult> upcomingResults = examResultRepository.findUpcomingExams(studentId);
            log.info("Found {} upcoming exams for student {}", upcomingResults.size(), studentId);
            
            if (upcomingResults.isEmpty()) {
                log.warn("No upcoming exams found for student: {}", studentId);
                return new ArrayList<>();
            }
            
            return upcomingResults.stream()
                    .map(result -> {
                        try {
                            return mapToUpcomingExamResponse(result);
                        } catch (Exception e) {
                            log.error("Error mapping exam result: {}", e.getMessage(), e);
                            throw new RuntimeException("Error processing exam: " + e.getMessage(), e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching upcoming exams for student {}: {}", studentId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch upcoming exams: " + e.getMessage(), e);
        }
    }

    /**
     * 📌 Get Completed Exams
     */
    public List<CompletedExamResponse> getCompletedExams(Long studentId) {
        log.info("Fetching completed exams for student: {}", studentId);
        
        try {
            List<ExamResult> completedResults = examResultRepository.findCompletedExams(studentId);
            log.info("Found {} completed exams for student {}", completedResults.size(), studentId);
            
            if (completedResults.isEmpty()) {
                log.warn("No completed exams found for student: {}", studentId);
                return new ArrayList<>();
            }
            
            return completedResults.stream()
                    .map(result -> {
                        try {
                            return mapToCompletedExamResponse(result);
                        } catch (Exception e) {
                            log.error("Error mapping completed exam: {}", e.getMessage(), e);
                            throw new RuntimeException("Error processing exam: " + e.getMessage(), e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching completed exams for student {}: {}", studentId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch completed exams: " + e.getMessage(), e);
        }
    }

    /**
     * 📌 Get Exam Result Detail (Full View with Breakdown)
     */
    public ExamResultDetailResponse getExamResultDetail(Long examResultId, Long studentId) {
        log.info("Fetching exam result detail for examResult: {}, student: {}", examResultId, studentId);
        
        try {
            ExamResult examResult = examResultRepository.findById(examResultId)
                    .orElseThrow(() -> new RuntimeException("Exam result not found: " + examResultId));
            
            log.info("Found exam result: {} for student: {}", examResultId, examResult.getStudent().getId());
            
            // Verify student ownership
            if (!examResult.getStudent().getId().equals(studentId)) {
                log.error("Unauthorized access: Student {} trying to access result for student {}", 
                    studentId, examResult.getStudent().getId());
                throw new RuntimeException("Unauthorized access to exam result");
            }
            
            if (examResult.getTotalMarks() == null || examResult.getTotalMarks() == 0) {
                log.error("Invalid total marks for exam result: {}", examResultId);
                throw new RuntimeException("Invalid exam result data: total marks is zero or null");
            }
            
            int percentage = (examResult.getObtainedMarks() * 100) / examResult.getTotalMarks();
            String grade = calculateGrade(percentage);
            
            List<ExamResultDetailResponse.MarksBreakdownSection> breakdown = 
                    getMarksBreakdown(examResultId);
            
            PerformanceInsightsResponse performanceInsights = 
                    getPerformanceInsights(examResult);
            
            TeacherFeedbackResponse feedback = 
                    getTeacherFeedback(examResultId);
            
            List<QuestionWiseAnalysisResponse> questionAnalysis = 
                    getQuestionWiseAnalysis(examResultId);
            
            log.info("Successfully built exam result detail for examResult: {}", examResultId);
            
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
        } catch (Exception e) {
            log.error("Error fetching exam result detail for examResult {}: {}", examResultId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch exam result detail: " + e.getMessage(), e);
        }
    }

    // ...existing helper methods...

    private List<ExamResultDetailResponse.MarksBreakdownSection> getMarksBreakdown(Long examResultId) {
        try {
            List<ExamResultBreakdown> breakdowns = breakdownRepository.findByExamResultId(examResultId);
            
            if (breakdowns == null || breakdowns.isEmpty()) {
                log.warn("No marks breakdown found for examResultId: {}", examResultId);
                return new ArrayList<>();
            }
            
            return breakdowns.stream()
                    .map(bd -> ExamResultDetailResponse.MarksBreakdownSection.builder()
                            .sectionName(bd.getSectionName() != null ? bd.getSectionName() : "Section")
                            .obtainedMarks(bd.getObtainedMarks() != null ? bd.getObtainedMarks() : 0)
                            .totalMarks(bd.getTotalMarks() != null ? bd.getTotalMarks() : 0)
                            .percentage(bd.getPercentage() != null ? bd.getPercentage() : 0.0)
                            .visualPercentage(bd.getPercentage() != null ? bd.getPercentage().intValue() : 0)
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching marks breakdown: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private PerformanceInsightsResponse getPerformanceInsights(ExamResult examResult) {
        Object[] stats = examResultRepository.getExamPerformanceStats(examResult.getExam().getId());
        
        Double classAverage = stats[0] != null ? ((Number) stats[0]).doubleValue() : 0.0;
        Double highestScore = stats[1] != null ? ((Number) stats[1]).doubleValue() : 0.0;
        
        double studentScore = (examResult.getObtainedMarks() * 100.0) / examResult.getTotalMarks();
        
        Long rankCount = examResultRepository.getStudentRank(examResult.getId(), examResult.getExam().getId());
        Integer rank = (int) (rankCount + 1);
        
        return PerformanceInsightsResponse.builder()
                .classAverage(classAverage.intValue())
                .highestScore(highestScore.intValue())
                .yourRank(rank)
                .totalStudents(30)
                .rankText("#" + rank)
                .performanceLabel(getPerformanceLabel(studentScore))
                .comparisonWithAverage((int) (studentScore - classAverage))
                .isAboveAverage(studentScore >= classAverage)
                .build();
    }

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

    private List<QuestionWiseAnalysisResponse> getQuestionWiseAnalysis(Long examResultId) {
        List<StudentExamAnswer> answers = answerRepository.findByExamResultId(examResultId);
        
        return answers.stream()
                .map(answer -> {
                    try {
                        ExamQuestion question = answer.getQuestion();
                        String sectionName = "General";
                        if (question != null && question.getSection() != null) {
                            sectionName = question.getSection().getName();
                        }
                        
                        return QuestionWiseAnalysisResponse.builder()
                                .questionNumber(question != null ? question.getQuestionNumber() : 0)
                                .questionText(question != null ? question.getQuestionText() : "")
                                .status(answer.getStatus())
                                .statusLabel(formatStatus(answer.getStatus()))
                                .marksObtained(answer.getObtainedMarks())
                                .totalMarks(question != null ? question.getMarks() : 0)
                                .section(sectionName)
                                .isCorrect("CORRECT".equals(answer.getStatus()))
                                .build();
                    } catch (Exception e) {
                        log.error("Error processing answer: {}", e.getMessage(), e);
                        return QuestionWiseAnalysisResponse.builder()
                                .questionNumber(0)
                                .questionText("Error")
                                .status("ERROR")
                                .statusLabel("Error")
                                .marksObtained(0)
                                .totalMarks(0)
                                .section("Error")
                                .isCorrect(false)
                                .build();
                    }
                })
                .collect(Collectors.toList());
    }

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
                .topics("Topics")
                .examType("Written")
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

    // Legacy methods for backward compatibility
    public List<CompletedExamResponse> getCompleted(Long studentId) {
        return getCompletedExams(studentId);
    }

    public ExamDetailResponse getResult(Long examId, Long studentId) {
        ExamResult er = examResultRepository
                .findByExam_IdAndStudent_Id(examId, studentId)
                .orElseThrow(() -> new RuntimeException("Result not found"));

        int percent = (er.getObtainedMarks() * 100) / er.getTotalMarks();

        return new ExamDetailResponse(
                er.getSubject().getName(),
                er.getExam().getName(),
                er.getObtainedMarks() + "/" + er.getTotalMarks(),
                calculateGrade(percent),
                er.getExam().getExamDate().toString(),
                er.getRank(),
                er.getFeedback()
        );
    }
}