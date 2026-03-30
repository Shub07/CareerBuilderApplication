package com.org.careerbuilder.service;

import com.org.careerbuilder.models.StudentPerformanceReport;
import java.util.List;
import java.util.Optional;

public interface StudentPerformanceReportService {
    
    // ==================== CREATE ====================
    StudentPerformanceReport addPerformanceReport(StudentPerformanceReport report);
    
    // ==================== READ ====================
    List<StudentPerformanceReport> getAllReports();
    
    StudentPerformanceReport getReportById(Long id);
    
    List<StudentPerformanceReport> getReportsByStudent(Long studentId);
    
    List<StudentPerformanceReport> getReportsByStudentAndYear(Long studentId, Integer academicYear);
    
    List<StudentPerformanceReport> getReportsByStudentAndSubject(Long studentId, Long subjectId);
    
    List<StudentPerformanceReport> getReportsByStudentAndExam(Long studentId, Long examId);
    
    List<StudentPerformanceReport> getReportsBySubject(Long subjectId);
    
    List<StudentPerformanceReport> getReportsByExam(Long examId);
    
    // ==================== CALCULATIONS ====================
    Optional<Double> getOverallAverageByStudent(Long studentId, Integer academicYear);
    
    Optional<Double> getSubjectAverageByStudent(Long studentId, Long subjectId);
    
    Optional<StudentPerformanceReport> getBestSubjectByStudent(Long studentId, Integer academicYear);
    
    Double getPerformanceTrend(Long studentId, Integer academicYear);
    
    // ==================== UPDATE ====================
    StudentPerformanceReport updatePerformanceReport(Long id, StudentPerformanceReport reportData);
    
    // ==================== DELETE ====================
    void deletePerformanceReport(Long id);
    
    void deleteReportsByStudent(Long studentId);
}

