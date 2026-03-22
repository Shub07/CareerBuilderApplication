package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ClassSubjectTeacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassSubjectTeacherRepository extends JpaRepository<ClassSubjectTeacher, Long> {
    List<ClassSubjectTeacher> findBySchoolIdAndClassNameAndSectionAndActiveTrue(Long schoolId, String className, String section);
    List<ClassSubjectTeacher> findBySchoolIdAndClassNameAndSectionAndSubject_IdAndActiveTrue(Long schoolId, String className, String section, Long subjectId);
}