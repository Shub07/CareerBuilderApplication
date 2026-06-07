package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.AdmissionEnquiry;
import com.org.careerbuilder.models.enums.AdmissionEnquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AdmissionEnquiryRepository extends JpaRepository<AdmissionEnquiry, Long> {

    List<AdmissionEnquiry> findBySchool_IdOrderByCreatedAtDesc(Long schoolId);

    List<AdmissionEnquiry> findBySchool_IdAndStatusOrderByCreatedAtDesc(Long schoolId, AdmissionEnquiryStatus status);

    long countBySchool_IdAndStatus(Long schoolId, AdmissionEnquiryStatus status);

    long countBySchool_IdAndCreatedAtGreaterThanEqual(Long schoolId, LocalDateTime from);
}
