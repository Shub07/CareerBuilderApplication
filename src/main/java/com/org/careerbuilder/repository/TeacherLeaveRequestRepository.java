package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.TeacherLeaveRequest;
import com.org.careerbuilder.models.enums.TeacherLeaveStatus;
import com.org.careerbuilder.models.enums.TeacherLeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface TeacherLeaveRequestRepository extends JpaRepository<TeacherLeaveRequest, Long> {
    List<TeacherLeaveRequest> findByFaculty_IdAndStatusInAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            Long facultyId, Collection<TeacherLeaveStatus> statuses, LocalDate date1, LocalDate date2);

    long countByFaculty_IdAndLeaveTypeAndStatusIn(Long facultyId, TeacherLeaveType leaveType, Collection<TeacherLeaveStatus> statuses);

    List<TeacherLeaveRequest> findByFaculty_IdOrderByCreatedAtDesc(Long facultyId);

    List<TeacherLeaveRequest> findByFaculty_IdAndStatusOrderByCreatedAtDesc(Long facultyId, TeacherLeaveStatus status);

    List<TeacherLeaveRequest> findBySchoolIdOrderByCreatedAtDesc(Long schoolId);

    List<TeacherLeaveRequest> findBySchoolIdAndStatusOrderByCreatedAtDesc(Long schoolId, TeacherLeaveStatus status);
}
