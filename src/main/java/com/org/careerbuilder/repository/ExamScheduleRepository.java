package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ExamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {

    List<ExamSchedule> findByExam_IdAndSchoolIdOrderByScheduledDateAscStartTimeAsc(Long examId, Long schoolId);

    Optional<ExamSchedule> findByIdAndSchoolId(Long id, Long schoolId);

    long countByExam_IdAndSchoolId(Long examId, Long schoolId);

    @Query("""
            SELECT COUNT(s) FROM ExamSchedule s
            WHERE s.schoolId = :schoolId
            AND s.venueRef.id = :venueId
            AND s.scheduledDate = :date
            AND s.id <> COALESCE(:excludeId, -1)
            AND s.startTime < :endTime AND s.endTime > :startTime
            """)
    long countVenueConflicts(
            @Param("schoolId") Long schoolId,
            @Param("venueId") Long venueId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);
}
