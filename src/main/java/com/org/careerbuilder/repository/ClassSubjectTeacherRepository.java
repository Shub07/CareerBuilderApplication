package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ClassSubjectTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassSubjectTeacherRepository extends JpaRepository<ClassSubjectTeacher, Long> {
    List<ClassSubjectTeacher> findBySchoolIdAndClassNameAndSectionAndActiveTrue(Long schoolId, String className, String section);
    List<ClassSubjectTeacher> findBySchoolIdAndClassNameAndSectionAndSubject_IdAndActiveTrue(Long schoolId, String className, String section, Long subjectId);

    List<ClassSubjectTeacher> findByFaculty_IdAndActiveTrue(Long facultyId);

    @Query("""
            SELECT DISTINCT cst FROM ClassSubjectTeacher cst
            JOIN FETCH cst.subject
            WHERE cst.faculty.id = :facultyId AND cst.active = true
            """)
    List<ClassSubjectTeacher> findByFaculty_IdAndActiveTrueWithSubject(@Param("facultyId") Long facultyId);

    @Query("""
            SELECT cst FROM ClassSubjectTeacher cst
            JOIN FETCH cst.subject
            WHERE cst.faculty.id IN :facultyIds AND cst.active = true
            """)
    List<ClassSubjectTeacher> findActiveByFacultyIds(@Param("facultyIds") List<Long> facultyIds);

    Optional<ClassSubjectTeacher> findByIdAndFaculty_Id(Long id, Long facultyId);

    @Query("""
            SELECT cst FROM ClassSubjectTeacher cst
            JOIN FETCH cst.subject
            JOIN FETCH cst.faculty
            WHERE cst.schoolId = :schoolId AND cst.className = :className AND cst.active = true
            ORDER BY cst.role, cst.faculty.firstName
            """)
    List<ClassSubjectTeacher> findActiveByClass(@Param("schoolId") Long schoolId,
                                                @Param("className") String className);

    Optional<ClassSubjectTeacher> findByIdAndSchoolId(Long id, Long schoolId);
}