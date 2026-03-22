package com.org.careerbuilder.website.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.careerbuilder.website.models.Partner;

public interface PartnerRepository extends JpaRepository<Partner, Long> {

    List<Partner> findAllByActiveTrueOrderByNameAsc();
}
