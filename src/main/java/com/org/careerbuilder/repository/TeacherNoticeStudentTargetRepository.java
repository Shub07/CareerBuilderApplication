package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.TeacherNoticeStudentTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherNoticeStudentTargetRepository extends JpaRepository<TeacherNoticeStudentTarget, Long> {
    List<TeacherNoticeStudentTarget> findByTeacherNotice_Id(Long teacherNoticeId);

    void deleteByTeacherNotice_Id(Long teacherNoticeId);
}
