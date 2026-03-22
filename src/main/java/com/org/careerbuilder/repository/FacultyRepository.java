package com.org.careerbuilder.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.careerbuilder.models.Faculty;


public interface FacultyRepository extends JpaRepository<Faculty, Long> {}
