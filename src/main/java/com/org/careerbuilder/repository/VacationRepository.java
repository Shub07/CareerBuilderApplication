package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.Vacation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 🎫 Vacation Repository - Data access for vacation operations
 */
@Repository
public interface VacationRepository extends JpaRepository<Vacation, Long> {

    // ===== Basic Queries =====
    
    @Query("""
        SELECT v FROM Vacation v
        WHERE v.school.id = :schoolId
        ORDER BY v.startDate DESC
    """)
    Page<Vacation> findBySchoolId(@Param("schoolId") Long schoolId, Pageable pageable);

    @Query("""
        SELECT v FROM Vacation v
        WHERE v.school.id = :schoolId
        ORDER BY v.startDate DESC
    """)
    List<Vacation> findAllBySchoolId(@Param("schoolId") Long schoolId);

    // ===== Status-based Queries =====

    @Query("""
        SELECT v FROM Vacation v
        WHERE v.school.id = :schoolId AND v.isActive = true
        ORDER BY v.startDate DESC
    """)
    List<Vacation> findActiveVacationsBySchoolId(@Param("schoolId") Long schoolId);

    // ===== Temporal Queries =====

    @Query("""
        SELECT v FROM Vacation v
        WHERE v.school.id = :schoolId 
        AND v.startDate >= CURRENT_DATE
        AND v.isActive = true
        ORDER BY v.startDate ASC
    """)
    List<Vacation> findUpcomingVacations(@Param("schoolId") Long schoolId);

    @Query("""
        SELECT v FROM Vacation v
        WHERE v.school.id = :schoolId 
        AND CURRENT_DATE BETWEEN v.startDate AND v.endDate
        AND v.isActive = true
        ORDER BY v.startDate ASC
    """)
    List<Vacation> findOngoingVacations(@Param("schoolId") Long schoolId);

    @Query("""
        SELECT v FROM Vacation v
        WHERE v.school.id = :schoolId 
        AND v.endDate < CURRENT_DATE
        ORDER BY v.startDate DESC
    """)
    List<Vacation> findCompletedVacations(@Param("schoolId") Long schoolId);

    // ===== Date Range Queries =====

    @Query("""
        SELECT v FROM Vacation v
        WHERE v.school.id = :schoolId
        AND v.startDate <= :endDate
        AND v.endDate >= :startDate
        AND v.isActive = true
        ORDER BY v.startDate ASC
    """)
    List<Vacation> findVacationsBetweenDates(
            @Param("schoolId") Long schoolId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ===== Type-based Queries =====

    @Query("""
        SELECT v FROM Vacation v
        WHERE v.school.id = :schoolId 
        AND v.vacationType = :type
        AND v.isActive = true
        ORDER BY v.startDate DESC
    """)
    List<Vacation> findBySchoolIdAndType(
            @Param("schoolId") Long schoolId,
            @Param("type") Vacation.VacationType type);

    @Query("""
        SELECT v FROM Vacation v
        WHERE v.school.id = :schoolId 
        AND v.vacationType = :type
        AND v.startDate >= CURRENT_DATE
        ORDER BY v.startDate ASC
    """)
    List<Vacation> findUpcomingByType(
            @Param("schoolId") Long schoolId,
            @Param("type") Vacation.VacationType type);

    // ===== Notification Queries =====

    @Query("""
        SELECT v FROM Vacation v
        WHERE v.school.id = :schoolId
        AND v.noticeSent = false
        AND v.startDate > CURRENT_DATE
        AND v.isActive = true
        ORDER BY v.startDate ASC
    """)
    List<Vacation> findVacationsWithoutNotice(@Param("schoolId") Long schoolId);

    // ===== Existence Checks =====

    @Query("""
        SELECT COUNT(v) > 0 FROM Vacation v
        WHERE v.school.id = :schoolId
        AND CURRENT_DATE BETWEEN v.startDate AND v.endDate
        AND v.isActive = true
    """)
    boolean isSchoolOnVacation(@Param("schoolId") Long schoolId);

    Optional<Vacation> findByIdAndSchoolId(Long vacationId, Long schoolId);
}

