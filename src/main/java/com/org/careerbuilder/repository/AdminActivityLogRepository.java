package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.AdminActivityLog;
import com.org.careerbuilder.models.enums.AdminActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdminActivityLogRepository extends JpaRepository<AdminActivityLog, Long> {

    List<AdminActivityLog> findBySchool_IdOrderByCreatedAtDesc(Long schoolId, Pageable pageable);

    List<AdminActivityLog> findBySchool_IdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
            Long schoolId, String entityType, Long entityId, Pageable pageable);

    @Query("""
            SELECT l FROM AdminActivityLog l
            WHERE l.school.id = :schoolId
            AND l.entityType = :entityType
            AND l.entityId = :entityId
            AND (:activityType IS NULL OR l.activityType = :activityType)
            AND (:q IS NULL OR :q = '' OR LOWER(l.title) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(l.description) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(COALESCE(l.performedBy, '')) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY l.createdAt DESC
            """)
    Page<AdminActivityLog> searchStudentActivityLog(
            @Param("schoolId") Long schoolId,
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId,
            @Param("q") String q,
            @Param("activityType") AdminActivityType activityType,
            Pageable pageable);

    @Query("""
            SELECT l FROM AdminActivityLog l
            WHERE l.school.id = :schoolId
            AND l.entityType = :entityType
            AND l.entityId = :entityId
            AND l.activityType IN :activityTypes
            AND (:q IS NULL OR :q = '' OR LOWER(l.title) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(l.description) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(COALESCE(l.performedBy, '')) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY l.createdAt DESC
            """)
    Page<AdminActivityLog> searchStudentActivityLogByTypes(
            @Param("schoolId") Long schoolId,
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId,
            @Param("q") String q,
            @Param("activityTypes") List<AdminActivityType> activityTypes,
            Pageable pageable);
}
