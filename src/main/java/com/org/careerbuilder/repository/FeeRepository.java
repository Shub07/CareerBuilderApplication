package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {

    List<Fee> findByStudentId(Long studentId);

    @Query("""
        SELECT SUM(f.totalAmount) FROM Fee f WHERE f.studentId = :studentId
    """)
    Double getTotalFees(Long studentId);

    @Query("""
        SELECT SUM(p.amount) FROM Payment p
        JOIN Fee f ON p.feeId = f.id
        WHERE f.studentId = :studentId AND p.status = 'SUCCESS'
    """)
    Double getPaidAmount(Long studentId);
}
