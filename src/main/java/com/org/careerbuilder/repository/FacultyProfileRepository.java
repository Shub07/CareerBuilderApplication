package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.FacultyProfile;
import com.org.careerbuilder.models.enums.FacultyAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FacultyProfileRepository extends JpaRepository<FacultyProfile, Long> {

    Optional<FacultyProfile> findByFaculty_Id(Long facultyId);

    @Query("""
            SELECT COUNT(fp) FROM FacultyProfile fp
            JOIN fp.faculty f
            WHERE f.school.id = :schoolId AND fp.deleted = false
            AND fp.accountStatus = :status
            """)
    long countBySchoolAndStatus(
            @Param("schoolId") Long schoolId,
            @Param("status") FacultyAccountStatus status);

    @Query("""
            SELECT COUNT(f) FROM Faculty f
            WHERE f.school.id = :schoolId
            AND NOT EXISTS (
                SELECT 1 FROM ClassSubjectTeacher cst
                WHERE cst.faculty.id = f.id AND cst.active = true
            )
            AND EXISTS (
                SELECT 1 FROM FacultyProfile fp
                WHERE fp.faculty.id = f.id AND fp.deleted = false
            )
            """)
    long countUnassignedBySchoolId(@Param("schoolId") Long schoolId);
}
