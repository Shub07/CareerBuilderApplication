package com.org.careerbuilder.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name="class_schedule_slots",
        indexes = @Index(name="idx_schedule_lookup", columnList="school_id,class_name,section,day_of_week,start_time"))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassScheduleSlot {

    public enum SlotType { CLASS, BREAK, LUNCH, SPECIAL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="school_id", nullable=false)
    private Long schoolId;

    @Column(name="class_name", nullable=false, length=20)
    private String className;

    @Column(name="section", nullable=false, length=5)
    private String section;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="subject_id", nullable=false)
    private Subject subject;

    @Column(name="day_of_week", nullable=false)
    private Integer dayOfWeek; // 1=Mon ... 7=Sun

    @Column(name="start_time", nullable=false)
    private LocalTime startTime;

    @Column(name="end_time", nullable=false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name="slot_type", nullable=false, length=20)
    private SlotType slotType;

    @Column(name="title", length=120)
    private String title;

    @Column(name="is_active", nullable=false)
    private boolean active = true;
}