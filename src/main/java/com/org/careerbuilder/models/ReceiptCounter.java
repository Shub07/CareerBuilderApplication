package com.org.careerbuilder.models;

import jakarta.persistence.*;
import lombok.*;

/**
 * Per-year counter backing race-free receipt-number generation.
 * Bumped atomically via an INSERT ... ON CONFLICT ... RETURNING upsert.
 */
@Entity
@Table(name = "receipt_counters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptCounter {

    @Id
    @Column(name = "year")
    private Integer year;

    @Column(name = "last_value", nullable = false)
    private Long lastValue;
}
