package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.AttendanceRecord;
import com.org.careerbuilder.models.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    long countByStudent_IdAndDateBetween(Long studentId, LocalDate from, LocalDate to);

    long countByStudent_IdAndStatusAndDateBetween(Long studentId, AttendanceStatus status,
                                                  LocalDate from, LocalDate to);

    Page<AttendanceRecord> findByStudent_IdAndDateBetween(Long studentId, LocalDate from, LocalDate to, Pageable pageable);

    Page<AttendanceRecord> findByStudent_IdAndDateBetweenOrderByDateDesc(
            Long studentId, LocalDate from, LocalDate to, Pageable pageable);

    Page<AttendanceRecord> findByClassNameAndSectionAndDateBetween(String className, String section, LocalDate from, LocalDate to, Pageable pageable);

    List<AttendanceRecord> findByDateAndClassNameAndSection(LocalDate date, String className, String section);

    List<AttendanceRecord> findByDateAndStatus(LocalDate date, AttendanceStatus status);

    Page<AttendanceRecord> findByStatusAndDateBetween(AttendanceStatus status, LocalDate from, LocalDate to, Pageable pageable);

    @Query("SELECT a FROM AttendanceRecord a WHERE a.date = :date AND a.student.id = :studentId")
    AttendanceRecord findByDateAndStudentId(LocalDate date, Long studentId);

    @Query("SELECT a FROM AttendanceRecord a WHERE a.date BETWEEN :from AND :to AND a.student.id IN (:studentIds)")
    List<AttendanceRecord> findByDateRangeAndStudentIds(LocalDate from, LocalDate to, List<Long> studentIds);

    @Query("SELECT a FROM AttendanceRecord a WHERE a.date = :date AND a.student.id IN (:studentIds)")
    List<AttendanceRecord> findByDateAndStudent_IdIn(@Param("date") LocalDate date, @Param("studentIds") Collection<Long> studentIds);

    long countByStudent_IdAndStatus(Long studentId, AttendanceStatus status);
}
