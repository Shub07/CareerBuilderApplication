package com.org.careerbuilder.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.careerbuilder.models.School;

public interface SchoolRepository extends JpaRepository<School, Long> {
}