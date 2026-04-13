package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Attendance settings for a school
 */
@Entity
@Table(name = "attendance_settings")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AttendanceSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Long id;

    @NotNull
    @Column(name = "school_id", unique = true, nullable = false)
    private Long schoolId;

    @Column(name = "working_days_per_week", nullable = false)
    private Integer workingDaysPerWeek = 5;

    @Column(name = "min_attendance_percentage", nullable = false)
    private Integer minAttendancePercentage = 75;

    @Column(name = "allow_bulk_marking", nullable = false)
    private Boolean allowBulkMarking = true;

    @Column(name = "allow_retroactive_marking", nullable = false)
    private Boolean allowRetroactiveMarking = true;

    @Column(name = "max_retroactive_days", nullable = false)
    private Integer maxRetroactiveDays = 7;

    @Column(name = "notify_low_attendance", nullable = false)
    private Boolean notifyLowAttendance = true;

    @Column(name = "low_attendance_threshold", nullable = false)
    private Integer lowAttendanceThreshold = 75;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
}

