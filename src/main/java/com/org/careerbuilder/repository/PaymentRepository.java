package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByFeeId(Long feeId);
}
