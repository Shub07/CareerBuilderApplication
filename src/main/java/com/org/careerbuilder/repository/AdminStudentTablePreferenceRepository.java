package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.AdminStudentTablePreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminStudentTablePreferenceRepository extends JpaRepository<AdminStudentTablePreference, Long> {

    Optional<AdminStudentTablePreference> findBySchoolIdAndAdminUserIdAndPreferenceKey(
            Long schoolId, Long adminUserId, String preferenceKey);

    Optional<AdminStudentTablePreference> findBySchoolIdAndAdminEmailIgnoreCaseAndPreferenceKey(
            Long schoolId, String adminEmail, String preferenceKey);
}
