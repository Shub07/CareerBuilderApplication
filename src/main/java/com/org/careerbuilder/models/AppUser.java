package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Authentication entity.
 * Stores login credentials and role information.
 * Linked to Student (for STUDENT role).
 */
@Entity
@Table(
        name = "app_users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_app_users_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_app_users_role", columnList = "role"),
                @Index(name = "idx_app_users_student_id", columnList = "student_id")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Required by JPA
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // Prevent misuse
@Builder
@ToString(exclude = {"student"}) // Avoid lazy loading issues in logs
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long id;

    /**
     * Unique login email.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    @Column(name = "email", nullable = false, length = 150)
    private String email;

    /**
     * BCrypt hashed password.
     * Never store plain text passwords.
     */
    @NotBlank(message = "Password hash is required")
    @Column(name = "password_hash", nullable = false, length = 120)
    private String passwordHash;

    /**
     * System role used for authorization.
     */
    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private UserRole role;

    /**
     * Linked student entity.
     * Used when role = STUDENT.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    private Student student;

    /**
     * Indicates whether the account is active.
     */
    @Column(name = "is_active", nullable = false)
    private boolean active;

    /**
     * Audit fields.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Set default values before insert.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    /**
     * Update timestamp before update.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}