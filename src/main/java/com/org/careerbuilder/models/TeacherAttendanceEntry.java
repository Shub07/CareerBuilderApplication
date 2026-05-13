package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.TeacherAttendanceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "teacher_attendance_entries",
        uniqueConstraints = @UniqueConstraint(name = "uk_teacher_attendance_faculty_date", columnNames = {"faculty_id", "work_date"}),
        indexes = {
                @Index(name = "idx_tae_faculty_date", columnList = "faculty_id, work_date"),
                @Index(name = "idx_tae_school_date", columnList = "school_id, work_date")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherAttendanceEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Column(name = "check_out_time")
    private LocalTime checkOutTime;

    @Column(name = "worked_minutes")
    private Integer workedMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TeacherAttendanceStatus status;

    @Column(name = "late_note", length = 500)
    private String lateNote;

    @Column(name = "work_summary", length = 1000)
    private String workSummary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
