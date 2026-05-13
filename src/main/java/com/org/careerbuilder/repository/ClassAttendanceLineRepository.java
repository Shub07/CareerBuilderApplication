package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ClassAttendanceLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassAttendanceLineRepository extends JpaRepository<ClassAttendanceLine, Long> {

    List<ClassAttendanceLine> findBySession_IdOrderByStudent_RollNoAsc(Long sessionId);

    void deleteBySession_Id(Long sessionId);
}
