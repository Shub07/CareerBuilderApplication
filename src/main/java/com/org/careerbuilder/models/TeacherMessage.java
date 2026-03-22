package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="teacher_messages")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherMessage {

    public enum Status { SENT, READ, ARCHIVED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="student_id", nullable=false)
    private Student student;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="faculty_id", nullable=false)
    private Faculty faculty;

    @NotBlank
    @Column(name="subject", nullable=false, length=150)
    private String subject;

    @NotBlank
    @Column(name="message", nullable=false, columnDefinition="TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false, length=20)
    private Status status;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = Status.SENT;
    }
}