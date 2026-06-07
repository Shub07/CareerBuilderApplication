package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.StudentFeePaymentLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentFeePaymentLinkRepository extends JpaRepository<StudentFeePaymentLink, Long> {

    Optional<StudentFeePaymentLink> findByToken(String token);
}
