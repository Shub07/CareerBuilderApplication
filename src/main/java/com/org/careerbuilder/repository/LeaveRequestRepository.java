package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.LeaveRequest;
import com.org.careerbuilder.models.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findTop1ByStudent_IdAndStatusInAndFromDateGreaterThanEqualOrderByFromDateAsc(
            Long studentId, List<LeaveStatus> statuses, LocalDate today
    );

    long countByStudent_IdAndStatusInAndFromDateGreaterThanEqual(Long studentId, List<LeaveStatus> statuses, LocalDate today);
}
