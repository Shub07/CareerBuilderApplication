package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.TeacherSessionNote;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TeacherSessionNoteRepository extends JpaRepository<TeacherSessionNote, Long> {

    Optional<TeacherSessionNote> findByFaculty_IdAndSessionKindAndRefIdAndSessionDate(
            Long facultyId, String sessionKind, Long refId, LocalDate sessionDate);

    @Query("""
            SELECT n FROM TeacherSessionNote n
            WHERE n.faculty.id = :facultyId
            AND n.className = :className
            AND n.section = :section
            AND (:subjectId IS NULL OR n.subject.id = :subjectId)
            AND (n.sessionDate < :sessionDate
                 OR (n.sessionDate = :sessionDate AND n.sessionStartTime < :sessionStart))
            AND n.topicCovered IS NOT NULL AND TRIM(n.topicCovered) <> ''
            ORDER BY n.sessionDate DESC, n.sessionStartTime DESC
            """)
    List<TeacherSessionNote> findRecentTopicsBefore(
            @Param("facultyId") Long facultyId,
            @Param("className") String className,
            @Param("section") String section,
            @Param("subjectId") Long subjectId,
            @Param("sessionDate") LocalDate sessionDate,
            @Param("sessionStart") LocalTime sessionStart,
            Pageable pageable);
}
