package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.TeacherLeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherLeaveBalanceRepository extends JpaRepository<TeacherLeaveBalance, Long> {
    Optional<TeacherLeaveBalance> findByFaculty_Id(Long facultyId);
}
