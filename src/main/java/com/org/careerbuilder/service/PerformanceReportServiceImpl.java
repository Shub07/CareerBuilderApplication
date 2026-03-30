package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.models.ExamResult;
import com.org.careerbuilder.repository.ExamResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerformanceReportServiceImpl implements PerformanceReportService {

    private final ExamResultRepository repository;

    /**
     * 📊 MAIN METHOD (Controller calls this)
     */
    @Override
    public PerformanceReportResponse getPerformanceReport(Long studentId, String examType) {

        // Convert examType String to ExamType enum if provided
        com.org.careerbuilder.models.enums.ExamType examTypeEnum = null;
        if (examType != null && !examType.isEmpty()) {
            try {
                // Validate and convert to enum
                examTypeEnum = com.org.careerbuilder.models.enums.ExamType.valueOf(examType.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid exam type, ignore and fetch all
                examTypeEnum = null;
            }
        }

        // 🔥 Aggregated subject performance
        List<Object[]> raw = repository.getSubjectPerformance(studentId, examTypeEnum);

        // 🔥 Detailed grouping (subject → test list)
        Map<String, List<ExamResult>> detailedMap =
                repository.findDetailedResults(studentId, examTypeEnum)
                        .stream()
                        .collect(Collectors.groupingBy(er -> er.getSubject().getName()));

        // 🔥 Build subject response
        List<SubjectPerformanceResponse> subjects = raw.stream()
                .map(obj -> {

                    String subject = (String) obj[0];
                    int obtained = ((Number) obj[1]).intValue();
                    int total = ((Number) obj[2]).intValue();

                    int percent = total == 0 ? 0 : (obtained * 100) / total;

                    List<TestScoreResponse> tests =
                            detailedMap.getOrDefault(subject, List.of())
                                    .stream()
                                    .map(er -> new TestScoreResponse(
                                            er.getExam().getExamType().name(),
                                            er.getExam().getExamDate().toString(),
                                            er.getObtainedMarks() + "/" + er.getTotalMarks(),
                                            er.getTotalMarks() == 0 ? 0 :
                                                    (er.getObtainedMarks() * 100) / er.getTotalMarks()
                                    ))
                                    .toList();

                    return new SubjectPerformanceResponse(
                            subject,
                            getGrade(percent),
                            percent,
                            obtained + "/" + total,
                            tests
                    );
                })
                .toList();

        // 🔥 Average score
        Double avg = repository.getAverageScore(studentId);
        double avgSafe = avg == null ? 0.0 : avg;

        // 🔥 Best subject
        String bestSubject = subjects.stream()
                .max(Comparator.comparingInt(SubjectPerformanceResponse::percentage))
                .map(SubjectPerformanceResponse::subject)
                .orElse("N/A");

        return new PerformanceReportResponse(
                getGrade(avgSafe),
                avgSafe,
                "+5%",   // later can calculate dynamically
                bestSubject,
                subjects
        );
    }

    /**
     * 🎯 Grade logic
     */
    private String getGrade(double p) {
        if (p >= 90) return "A";
        if (p >= 75) return "B+";
        if (p >= 60) return "B";
        return "C";
    }
}