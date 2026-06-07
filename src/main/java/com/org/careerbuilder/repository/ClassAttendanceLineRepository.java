package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ClassAttendanceLine;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClassAttendanceLineRepository extends JpaRepository<ClassAttendanceLine, Long> {

    List<ClassAttendanceLine> findBySession_IdOrderByStudent_RollNoAsc(Long sessionId);

    void deleteBySession_Id(Long sessionId);

    @Query("""
            SELECT l FROM ClassAttendanceLine l
            JOIN FETCH l.session s
            JOIN FETCH s.subject sub
            JOIN FETCH s.faculty f
            WHERE l.student.id = :studentId
            ORDER BY s.sessionDate DESC, s.id DESC
            """)
    List<ClassAttendanceLine> findByStudentIdWithSession(@Param("studentId") Long studentId);

    @Query("""
            SELECT l FROM ClassAttendanceLine l
            JOIN FETCH l.session s
            JOIN FETCH s.subject sub
            JOIN FETCH s.faculty f
            WHERE l.student.id = :studentId AND sub.id = :subjectId
            ORDER BY s.sessionDate DESC
            """)
    List<ClassAttendanceLine> findByStudentIdAndSubjectId(
            @Param("studentId") Long studentId,
            @Param("subjectId") Long subjectId);
}
