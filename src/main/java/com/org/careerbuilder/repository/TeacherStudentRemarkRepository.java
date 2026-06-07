package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.TeacherStudentRemark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherStudentRemarkRepository extends JpaRepository<TeacherStudentRemark, Long> {

    Optional<TeacherStudentRemark> findByStudent_IdAndFaculty_Id(Long studentId, Long facultyId);
}
