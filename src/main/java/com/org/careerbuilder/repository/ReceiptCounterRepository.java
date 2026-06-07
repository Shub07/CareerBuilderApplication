package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ReceiptCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptCounterRepository extends JpaRepository<ReceiptCounter, Integer> {

    /** Atomically bump and return the next sequence for the given year. */
    @Modifying(clearAutomatically = true)
    @Query(value = """
            INSERT INTO receipt_counters (year, last_value) VALUES (:year, 1)
            ON CONFLICT (year) DO UPDATE SET last_value = receipt_counters.last_value + 1
            RETURNING last_value
            """, nativeQuery = true)
    long nextValue(@Param("year") int year);
}
