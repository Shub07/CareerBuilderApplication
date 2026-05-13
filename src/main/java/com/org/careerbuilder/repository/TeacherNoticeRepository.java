package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.TeacherNotice;
import com.org.careerbuilder.models.enums.TeacherNoticeType;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeacherNoticeRepository extends JpaRepository<TeacherNotice, Long> {
    List<TeacherNotice> findByFaculty_IdOrderByCreatedAtDesc(Long facultyId);

    List<TeacherNotice> findByFaculty_IdAndNoticeTypeOrderByCreatedAtDesc(Long facultyId, TeacherNoticeType noticeType);

    @Query("""
            SELECT n FROM TeacherNotice n
            WHERE n.faculty.id = :facultyId
            AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(n.description) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY n.createdAt DESC
            """)
    List<TeacherNotice> searchByFaculty(@Param("facultyId") Long facultyId, @Param("q") String q, Pageable pageable);
}
