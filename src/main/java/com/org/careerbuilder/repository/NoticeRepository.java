package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.Notice;
import com.org.careerbuilder.models.enums.NoticeCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // Find top recent notices for school
    List<Notice> findTop10BySchoolIdOrderByCreatedAtDesc(Long schoolId);

    // Count total notices for school
    long countBySchoolId(Long schoolId);

    // Find all notices for school (paginated)
    Page<Notice> findBySchoolIdOrderByIsPinnedDescCreatedAtDesc(Long schoolId, Pageable pageable);

    // Find notices by category
    List<Notice> findBySchoolIdAndCategoryOrderByIsPinnedDescCreatedAtDesc(Long schoolId, NoticeCategory category);

    Page<Notice> findBySchoolIdAndCategoryOrderByIsPinnedDescCreatedAtDesc(Long schoolId, NoticeCategory category, Pageable pageable);

    // Find pinned notices
    List<Notice> findBySchoolIdAndIsPinnedTrueOrderByCreatedAtDesc(Long schoolId);

    // Find new notices (created within last N hours)
    @Query(value = "SELECT n FROM Notice n WHERE n.schoolId = :schoolId AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notice> findNewNotices(@Param("schoolId") Long schoolId, @Param("since") LocalDateTime since);

    // Count new notices
    @Query(value = "SELECT COUNT(n) FROM Notice n WHERE n.schoolId = :schoolId AND n.createdAt >= :since")
    long countNewNotices(@Param("schoolId") Long schoolId, @Param("since") LocalDateTime since);

    // Count pinned notices
    long countBySchoolIdAndIsPinnedTrue(Long schoolId);

    // Search notices by title or body
    @Query(value = "SELECT n FROM Notice n WHERE n.schoolId = :schoolId AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(n.body) LIKE LOWER(CONCAT('%', :query, '%'))) ORDER BY n.isPinned DESC, n.createdAt DESC")
    Page<Notice> searchNotices(@Param("schoolId") Long schoolId, @Param("query") String query, Pageable pageable);
}

