package com.org.careerbuilder.service;

import com.org.careerbuilder.models.LeaveRequest;
import com.org.careerbuilder.models.enums.LeaveStatus;
import com.org.careerbuilder.repository.LeaveRequestRepository;
import com.org.careerbuilder.service.LeaveMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveMetricsServiceImpl implements LeaveMetricsService {

    private final LeaveRequestRepository leaveRequestRepository;

    @Override
    public int getUpcomingLeaveCount(Long studentId, LocalDate today) {
        return (int) leaveRequestRepository.countByStudent_IdAndStatusInAndFromDateGreaterThanEqual(
                studentId, List.of(LeaveStatus.APPLIED, LeaveStatus.APPROVED), today
        );
    }

    @Override
    public String getNextLeaveText(Long studentId, LocalDate today) {
        List<LeaveRequest> next = leaveRequestRepository.findTop1ByStudent_IdAndStatusInAndFromDateGreaterThanEqualOrderByFromDateAsc(
                studentId, List.of(LeaveStatus.APPLIED, LeaveStatus.APPROVED), today
        );
        if (next.isEmpty()) return "-";
        LeaveRequest lr = next.get(0);
        return lr.getFromDate() + " to " + lr.getToDate();
    }
}
