package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.StudentProfile;
import com.org.careerbuilder.models.enums.StudentAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByAdmissionNumberIgnoreCase(String admissionNumber);

    boolean existsByAdmissionNumberIgnoreCase(String admissionNumber);

    long countByStudent_School_IdAndAdmissionDateGreaterThanEqual(Long schoolId, LocalDate fromDate);

    Optional<StudentProfile> findByStudent_Id(Long studentId);

    List<StudentProfile> findByStudent_IdIn(Collection<Long> studentIds);

    long countByStudent_School_IdAndAccountStatus(Long schoolId, StudentAccountStatus status);

    long countByStudent_School_IdAndAccountStatusAndAcademicYear(
            Long schoolId, StudentAccountStatus status, String academicYear);

    @Query("""
            SELECT COUNT(p) FROM StudentProfile p
            WHERE p.student.school.id = :schoolId
            AND p.accountStatus = 'NEW_ADMISSION'
            """)
    long countNewAdmissions(@Param("schoolId") Long schoolId);

    @Query("""
            SELECT COUNT(p) FROM StudentProfile p
            WHERE p.student.school.id = :schoolId
            AND p.accountStatus = com.org.careerbuilder.models.enums.StudentAccountStatus.NEW_ADMISSION
            AND (:academicYear IS NULL OR p.academicYear = :academicYear)
            """)
    long countNewAdmissionsByYear(@Param("schoolId") Long schoolId, @Param("academicYear") String academicYear);

    @Query("""
            SELECT DISTINCT p.academicYear FROM StudentProfile p
            WHERE p.student.school.id = :schoolId AND p.academicYear IS NOT NULL
            ORDER BY p.academicYear DESC
            """)
    List<String> findDistinctAcademicYears(@Param("schoolId") Long schoolId);
}
