package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.NoticeAudienceStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Set;

public interface NoticeAudienceStudentRepository extends JpaRepository<NoticeAudienceStudent, Long> {
    boolean existsByNotice_IdAndStudent_Id(Long noticeId, Long studentId);

    long countByNotice_Id(Long noticeId);

    Set<NoticeAudienceStudent> findByNotice_IdInAndStudent_Id(Collection<Long> noticeIds, Long studentId);

    void deleteByNotice_Id(Long noticeId);
}
