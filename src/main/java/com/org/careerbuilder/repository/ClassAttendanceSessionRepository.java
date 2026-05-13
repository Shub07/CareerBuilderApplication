package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ClassAttendanceSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface ClassAttendanceSessionRepository extends JpaRepository<ClassAttendanceSession, Long> {

    Optional<ClassAttendanceSession> findByFaculty_IdAndClassNameIgnoreCaseAndSectionIgnoreCaseAndSubject_IdAndSessionDate(
            Long facultyId, String className, String section, Long subjectId, LocalDate sessionDate);

    @Query("""
            SELECT s FROM ClassAttendanceSession s
            WHERE s.faculty.id = :facultyId
            AND s.sessionDate >= :from
            AND s.sessionDate <= :to
            AND (:className IS NULL OR LOWER(s.className) = LOWER(:className))
            AND (:section IS NULL OR LOWER(s.section) = LOWER(:section))
            AND (:subjectId IS NULL OR s.subject.id = :subjectId)
            ORDER BY s.sessionDate DESC, s.id DESC
            """)
    Page<ClassAttendanceSession> searchHistory(
            @Param("facultyId") Long facultyId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("className") String className,
            @Param("section") String section,
            @Param("subjectId") Long subjectId,
            Pageable pageable);
}
