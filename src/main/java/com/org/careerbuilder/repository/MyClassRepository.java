package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.MyClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyClassRepository extends JpaRepository<MyClass, Long> {
    List<MyClass> findByStudentIdAndClassNameAndSection(Long studentId, String className, String section);
    List<MyClass> findByStudentId(Long studentId);
    List<MyClass> findByClassNameAndSection(String className, String section);

    @Query(value = """
            SELECT mc.subject_id, s.subject_name, mc.class_name, mc.section
            FROM my_classes mc
            LEFT JOIN subjects s ON s.subject_id = mc.subject_id
            WHERE mc.student_id = :studentId
            ORDER BY mc.id DESC
            """, nativeQuery = true)
    List<Object[]> findSubjectCardsByStudentId(@Param("studentId") Long studentId);
}

