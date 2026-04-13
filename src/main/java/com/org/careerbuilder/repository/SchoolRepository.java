package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.School;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, Long> {
	Optional<School> findBySchoolCodeIgnoreCase(String schoolCode);
}