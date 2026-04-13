package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.AttendanceSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttendanceSettingsRepository extends JpaRepository<AttendanceSettings, Long> {
    Optional<AttendanceSettings> findBySchoolId(Long schoolId);
}

