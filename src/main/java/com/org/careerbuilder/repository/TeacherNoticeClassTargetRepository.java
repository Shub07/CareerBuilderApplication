package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.TeacherNoticeClassTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherNoticeClassTargetRepository extends JpaRepository<TeacherNoticeClassTarget, Long> {
    List<TeacherNoticeClassTarget> findByTeacherNotice_Id(Long teacherNoticeId);

    void deleteByTeacherNotice_Id(Long teacherNoticeId);
}
