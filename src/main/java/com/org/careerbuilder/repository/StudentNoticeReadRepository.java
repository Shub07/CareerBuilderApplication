package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.StudentNoticeRead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentNoticeReadRepository extends JpaRepository<StudentNoticeRead, Long> {

    long countByStudent_Id(Long studentId);

    boolean existsByStudent_IdAndNotice_Id(Long studentId, Long noticeId);
}
