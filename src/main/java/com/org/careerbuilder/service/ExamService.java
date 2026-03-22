package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.models.ExamResult;
import com.org.careerbuilder.repository.ExamResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamResultRepository repository;

    /**
     * 📌 Completed Exams
     */
    public List<CompletedExamResponse> getCompleted(Long studentId) {
        return repository.findCompletedExams(studentId)
                .stream()
                .map(er -> new CompletedExamResponse(
                        er.getExam().getId(),
                        er.getSubject().getName(),
                        er.getExam().getName(),
                        er.getObtainedMarks() + "/" + er.getTotalMarks(),
                        calculateGrade(er),
                        er.getExam().getExamDate().toString()
                ))
                .toList();
    }

    /**
     * 📌 View Result
     */
    public ExamDetailResponse getResult(Long examId, Long studentId) {

        ExamResult er = repository
                .findByExam_IdAndStudent_Id(examId, studentId)
                .orElseThrow(() -> new RuntimeException("Result not found"));

        int percent = (er.getObtainedMarks() * 100) / er.getTotalMarks();

        return new ExamDetailResponse(
                er.getSubject().getName(),
                er.getExam().getName(),
                er.getObtainedMarks() + "/" + er.getTotalMarks(),
                calculateGrade(er),
                er.getExam().getExamDate().toString(),
                er.getRank(),
                er.getFeedback()
        );
    }

    /**
     * 🎯 Grade Logic
     */
    private String calculateGrade(ExamResult er) {
        int percent = (er.getObtainedMarks() * 100) / er.getTotalMarks();

        if (percent >= 90) return "Grade A";
        if (percent >= 75) return "Grade B";
        if (percent >= 60) return "Grade C";
        return "Grade D";
    }
}