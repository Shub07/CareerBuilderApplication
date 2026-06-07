package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ClassSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassSubjectRepository extends JpaRepository<ClassSubject, Long> {

    @Query("""
            SELECT cs FROM ClassSubject cs
            JOIN FETCH cs.subject
            LEFT JOIN FETCH cs.assignedTeacher t
            WHERE cs.academicClass.id = :classId
            ORDER BY cs.subject.name
            """)
    List<ClassSubject> findByClassIdWithDetails(@Param("classId") Long classId);

    boolean existsByAcademicClass_IdAndSubject_Id(Long classId, Long subjectId);

    boolean existsByAcademicClass_IdAndSubjectCodeIgnoreCase(Long classId, String subjectCode);

    Optional<ClassSubject> findByIdAndAcademicClass_Id(Long id, Long classId);
}
