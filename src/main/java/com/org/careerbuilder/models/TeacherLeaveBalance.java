package com.org.careerbuilder.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_leave_balances",
        uniqueConstraints = @UniqueConstraint(name = "uk_tlb_faculty", columnNames = {"faculty_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherLeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @Column(name = "casual_total", nullable = false)
    private Integer casualTotal;

    @Column(name = "medical_total", nullable = false)
    private Integer medicalTotal;

    @Column(name = "half_day_total", nullable = false)
    private Integer halfDayTotal;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (casualTotal == null) casualTotal = 12;
        if (medicalTotal == null) medicalTotal = 10;
        if (halfDayTotal == null) halfDayTotal = 6;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
