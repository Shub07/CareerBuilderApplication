package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.LeaveRequest;
import com.org.careerbuilder.models.enums.LeaveStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findTop1ByStudent_IdAndStatusInAndFromDateGreaterThanEqualOrderByFromDateAsc(
            Long studentId, List<LeaveStatus> statuses, LocalDate today
    );

    long countByStudent_IdAndStatusInAndFromDateGreaterThanEqual(
            Long studentId, List<LeaveStatus> statuses, LocalDate today
    );

    Page<LeaveRequest> findByStudent_IdOrderByFromDateDesc(Long studentId, Pageable pageable);

    List<LeaveRequest> findByStudent_IdOrderByFromDateDesc(Long studentId);

    Page<LeaveRequest> findByStudent_IdAndStatusOrderByFromDateDesc(
            Long studentId, LeaveStatus status, Pageable pageable
    );

    long countByStudent_IdAndStatus(Long studentId, LeaveStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.student.id = :studentId AND lr.fromDate >= CURRENT_DATE ORDER BY lr.fromDate ASC")
    List<LeaveRequest> findUpcomingLeaveRequests(@Param("studentId") Long studentId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.student.id = :studentId AND lr.toDate < CURRENT_DATE ORDER BY lr.toDate DESC")
    List<LeaveRequest> findPastLeaveRequests(@Param("studentId") Long studentId, Pageable pageable);

    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.student.school.id = :schoolId AND lr.status = :status
            ORDER BY lr.createdAt DESC
            """)
    List<LeaveRequest> findBySchoolIdAndStatus(@Param("schoolId") Long schoolId, @Param("status") LeaveStatus status);

    long countByStudent_School_IdAndStatus(Long schoolId, LeaveStatus status);
}
