package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long>, JpaSpecificationExecutor<Assignment> {

    List<Assignment> findBySubjectId(Long subjectId);

    @Query("""
            SELECT a FROM Assignment a
            JOIN FETCH a.subject
            JOIN FETCH a.teacher
            WHERE a.teacher.id = :teacherId
            ORDER BY a.dueDate DESC
            """)
    List<Assignment> findByTeacher_IdOrderByDueDateDesc(@Param("teacherId") Long teacherId);

    @Query("""
            SELECT a FROM Assignment a
            JOIN FETCH a.subject
            JOIN FETCH a.teacher
            WHERE a.id = :assignmentId AND a.teacher.id = :teacherId
            """)
    Optional<Assignment> findByIdAndTeacher_Id(@Param("assignmentId") Long assignmentId,
                                               @Param("teacherId") Long teacherId);
}