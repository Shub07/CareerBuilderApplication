package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.Faculty;
import com.org.careerbuilder.models.enums.FacultyAccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    long countBySchool_Id(Long schoolId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByFacultyIdIgnoreCase(String facultyId);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Optional<Faculty> findByIdAndSchool_Id(Long id, Long schoolId);

    Optional<Faculty> findFirstByEmailIgnoreCase(String email);

    Optional<Faculty> findFirstByPhone(String phone);

    Optional<Faculty> findFirstByEmailIgnoreCaseAndSchool_Id(String email, Long schoolId);

    Optional<Faculty> findFirstByPhoneAndSchool_Id(String phone, Long schoolId);

    @Query("""
            SELECT f FROM Faculty f
            JOIN FETCH f.subject
            JOIN FacultyProfile fp ON fp.faculty.id = f.id
            WHERE f.school.id = :schoolId
            AND fp.deleted = false
            AND (:q IS NULL OR :q = '' OR LOWER(f.firstName) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(f.lastName) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(CONCAT(f.firstName, ' ', f.lastName)) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(f.facultyId) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(f.email) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(f.subject.name) LIKE LOWER(CONCAT('%', :q, '%')))
            AND (:subjectId IS NULL OR f.subject.id = :subjectId)
            AND (:status IS NULL OR fp.accountStatus = :status)
            AND (:unassignedOnly = false OR NOT EXISTS (
                SELECT 1 FROM ClassSubjectTeacher cst
                WHERE cst.faculty.id = f.id AND cst.active = true))
            ORDER BY f.firstName, f.lastName
            """)
    Page<Faculty> searchBySchool(
            @Param("schoolId") Long schoolId,
            @Param("q") String q,
            @Param("subjectId") Long subjectId,
            @Param("status") FacultyAccountStatus status,
            @Param("unassignedOnly") boolean unassignedOnly,
            Pageable pageable);

    @Query("""
            SELECT f FROM Faculty f
            JOIN FETCH f.subject
            JOIN FETCH f.school
            WHERE f.id = :id AND f.school.id = :schoolId
            """)
    Optional<Faculty> findByIdAndSchool_IdWithDetails(@Param("id") Long id, @Param("schoolId") Long schoolId);

    @Query("""
            SELECT f FROM Faculty f
            WHERE f.school.id = :schoolId AND f.id <> :excludeId
            ORDER BY f.firstName, f.lastName
            """)
    List<Faculty> findColleaguesBySchoolExcluding(@Param("schoolId") Long schoolId, @Param("excludeId") Long excludeId);
}
