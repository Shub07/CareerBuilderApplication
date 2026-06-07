package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ClassSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassSectionRepository extends JpaRepository<ClassSection, Long> {

    @Query("""
            SELECT s FROM ClassSection s
            LEFT JOIN FETCH s.sectionTeacher t
            WHERE s.academicClass.id = :classId
            ORDER BY s.name
            """)
    List<ClassSection> findByClassIdWithTeacher(@Param("classId") Long classId);

    long countByAcademicClass_Id(Long classId);

    long countByAcademicClass_School_IdAndAcademicClass_DeletedFalse(Long schoolId);

    boolean existsByAcademicClass_IdAndNameIgnoreCase(Long classId, String name);

    Optional<ClassSection> findByIdAndAcademicClass_Id(Long id, Long classId);
}
