package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.MyClass;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.models.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyClassRepository extends JpaRepository<MyClass, Long> {
    List<MyClass> findByStudentIdAndClassNameAndSection(Long studentId, String className, String section);
    List<MyClass> findByStudentId(Long studentId);
    List<MyClass> findByClassNameAndSection(String className, String section);
}

