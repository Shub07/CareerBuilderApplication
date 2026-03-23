package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findTop10BySchoolIdOrderByCreatedAtDesc(Long schoolId);

    long countBySchoolId(Long schoolId);
}
