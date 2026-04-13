package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.DailyLifeActivityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "daily_life_activities",
        indexes = {
                @Index(name = "idx_dla_student_date", columnList = "student_id, activity_date"),
                @Index(name = "idx_dla_student_type", columnList = "student_id, activity_type"),
                @Index(name = "idx_dla_date_completed", columnList = "activity_date, completed")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class DailyLifeActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id", nullable = false, updatable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 40)
    private DailyLifeActivityType activityType;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.durationMinutes = calculateDurationMinutes();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.durationMinutes = calculateDurationMinutes();
        this.updatedAt = LocalDateTime.now();
    }

    private int calculateDurationMinutes() {
        if (startTime == null || endTime == null) {
            return 0;
        }

        long minutes = Duration.between(startTime, endTime).toMinutes();
        if (minutes <= 0) {
            minutes += 24 * 60;
        }
        return (int) minutes;
    }
}