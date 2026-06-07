package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.AssignmentSubmission;
import com.org.careerbuilder.models.enums.AssignmentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository
        extends JpaRepository<AssignmentSubmission, Long> {

    List<AssignmentSubmission> findByStudent_Id(Long studentId);

    Optional<AssignmentSubmission> findByAssignment_IdAndStudent_Id(
            Long assignmentId, Long studentId);

    List<AssignmentSubmission> findByAssignment_Id(Long assignmentId);

    @Query("""
            SELECT s FROM AssignmentSubmission s
            JOIN FETCH s.student
            WHERE s.assignment.id = :assignmentId
            ORDER BY s.submittedAt ASC NULLS LAST
            """)
    List<AssignmentSubmission> findByAssignment_IdWithStudent(@Param("assignmentId") Long assignmentId);

    long countByAssignment_Id(Long assignmentId);

    void deleteByAssignment_Id(Long assignmentId);

    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.assignment.id = :aid AND s.filePath IS NOT NULL AND TRIM(s.filePath) <> ''")
    long countSubmittedWithFile(@Param("aid") Long assignmentId);

    long countByAssignment_IdAndStatus(Long assignmentId, AssignmentStatus status);

    @Query("""
            SELECT s FROM AssignmentSubmission s
            JOIN FETCH s.assignment a
            JOIN FETCH a.subject sub
            JOIN FETCH a.teacher
            WHERE s.student.id = :studentId
            ORDER BY a.dueDate DESC
            """)
    List<AssignmentSubmission> findWithAssignmentForStudent(@Param("studentId") Long studentId);

    /**
     * Assignment buckets with submissions that still need grading (file uploaded, not GRADED).
     */
    @Query("""
            select a.title, count(s), max(s.submittedAt)
            from AssignmentSubmission s
            join s.assignment a
            where a.teacher.id = :facultyId
              and s.status in :statuses
              and s.filePath is not null
              and trim(s.filePath) <> ''
            group by a.id, a.title
            order by max(s.submittedAt) desc
            """)
    List<Object[]> aggregatePendingGradingByAssignment(
            @Param("facultyId") Long facultyId,
            @Param("statuses") List<AssignmentStatus> statuses,
            Pageable pageable);
}