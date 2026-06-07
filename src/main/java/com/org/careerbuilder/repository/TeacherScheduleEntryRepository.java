package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.TeacherScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TeacherScheduleEntryRepository extends JpaRepository<TeacherScheduleEntry, Long> {

    @Query("""
            SELECT e FROM TeacherScheduleEntry e
            WHERE e.faculty.id = :facultyId
            AND (
                e.specificDate = :date
                OR (e.recurring = true AND e.dayOfWeek = :dow)
            )
            ORDER BY e.startTime
            """)
    List<TeacherScheduleEntry> findApplicableForDate(
            @Param("facultyId") Long facultyId,
            @Param("date") LocalDate date,
            @Param("dow") int dayOfWeek);

    @Query("""
            SELECT e FROM TeacherScheduleEntry e
            WHERE e.faculty.id = :facultyId
            AND (e.recurring = true OR (e.recurring = false AND e.specificDate BETWEEN :from AND :to))
            ORDER BY e.recurring DESC, e.specificDate, e.dayOfWeek, e.startTime
            """)
    List<TeacherScheduleEntry> findRelevantForWeek(
            @Param("facultyId") Long facultyId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT COUNT(e) FROM TeacherScheduleEntry e
            WHERE e.faculty.id = :facultyId
            AND (
                e.specificDate = :date
                OR (e.recurring = true AND e.dayOfWeek = :dow)
            )
            AND e.startTime < :endTime AND e.endTime > :startTime
            AND (:excludeId IS NULL OR e.id <> :excludeId)
            """)
    long countOverlapping(
            @Param("facultyId") Long facultyId,
            @Param("date") LocalDate date,
            @Param("dow") int dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);
}
