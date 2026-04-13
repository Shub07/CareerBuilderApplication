package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 👨‍👩‍👧 Parent Credential Model - Parent Authentication & Authorization
 */
@Entity
@Table(name = "parent_credentials", indexes = {
    @Index(name = "idx_parent_email", columnList = "email"),
    @Index(name = "idx_parent_id", columnList = "parent_id"),
    @Index(name = "idx_student_id", columnList = "student_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentCredential {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String parentId;
    
    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @NotBlank
    @Column(nullable = false)
    private String passwordHash;
    
    @NotBlank
    @Column(nullable = false, length = 50)
    private String studentId;  // Links to student ID
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    private String role = "PARENT";
}

