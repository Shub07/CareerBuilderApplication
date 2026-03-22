package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    long countByStudent_IdAndDateBetween(Long studentId, LocalDate from, LocalDate to);

    long countByStudent_IdAndStatusAndDateBetween(Long studentId, com.org.careerbuilder.models.enums.AttendanceStatus status,
                                                  LocalDate from, LocalDate to);
}
