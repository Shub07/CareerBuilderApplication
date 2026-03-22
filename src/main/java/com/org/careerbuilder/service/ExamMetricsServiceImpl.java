package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.DashboardResponse;
import com.org.careerbuilder.models.ExamResult;
import com.org.careerbuilder.repository.ExamResultRepository;
import com.org.careerbuilder.service.ExamMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamMetricsServiceImpl implements ExamMetricsService {

    private final ExamResultRepository examResultRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @Override
    public DashboardResponse.RecentExamScore getRecentExamScore(Long studentId) {
        List<ExamResult> results =
                examResultRepository.findRecentResults(
                        studentId,
                        PageRequest.of(0, 6)
                );

        int totalObtained = results.stream().mapToInt(ExamResult::getObtainedMarks).sum();
        int totalMax = results.stream().mapToInt(ExamResult::getTotalMarks).sum();

        List<DashboardResponse.ExamSubjectScore> subjects = results.stream().map(r -> {
            int percent = r.getTotalMarks() == 0 ? 0 : (int) Math.round((r.getObtainedMarks() * 100.0) / r.getTotalMarks());
            String grade = percent >= 90 ? "Grade A" : percent >= 75 ? "Grade B" : percent >= 60 ? "Grade C" : "Grade D";
            String deltaText = r.getDeltaPercent() == null ? "" : "+" + r.getDeltaPercent();
            return new DashboardResponse.ExamSubjectScore(
                    r.getSubject().getName(),
                    grade,
                    deltaText,
                    r.getObtainedMarks() + "/" + r.getTotalMarks(),
                    r.getExam().getExamDate().format(DATE_FMT),
                    percent
            );
        }).toList();

        return new DashboardResponse.RecentExamScore(
                totalObtained + "/" + totalMax,
                subjects
        );
    }

    @Override
    public int getUpcomingExamsCount(String className, String section) {
        // Connect this later when you add class_exam_schedule mapping
        return 0;
    }

    @Override
    public String getNextExamText(String className, String section) {
        // Connect this later when you add class_exam_schedule mapping
        return "-";
    }
}
