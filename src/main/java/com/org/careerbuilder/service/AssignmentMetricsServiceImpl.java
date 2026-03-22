package com.org.careerbuilder.service;

import com.org.careerbuilder.repository.AssignmentSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AssignmentMetricsServiceImpl implements AssignmentMetricsService{

    private final AssignmentSubmissionRepository submissionRepository;

    public int getTotalAssignments(Long studentId) {
        return submissionRepository.findByStudent_Id(studentId).size();
    }

    @Override
    public int getDueThisWeek(Long studentId, LocalDate start, LocalDate end) {
        return (int) submissionRepository.findByStudent_Id(studentId)
                .stream()
                .filter(s -> {
                    LocalDate due = s.getAssignment().getDueDate();
                    return due != null && !due.isBefore(start) && !due.isAfter(end);
                })
                .count();
    }
}
