package com.org.careerbuilder.website.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.careerbuilder.website.models.DemoRequest;

public interface DemoRequestRepository extends JpaRepository<DemoRequest, Long> {
}
