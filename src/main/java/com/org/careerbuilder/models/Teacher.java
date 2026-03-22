package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(
        name = "teachers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_teacher_email", columnNames = "teacher_email"),
                @UniqueConstraint(name = "uk_teacher_phone", columnNames = "teacher_phone")
        }
)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teacher_id", nullable = false, updatable = false)
    private Long id;

    @NotBlank @Size(min = 2, max = 120)
    @Column(name = "teacher_name", nullable = false, length = 120)
    private String name;

    @NotBlank @Email @Size(max = 150)
    @Column(name = "teacher_email", nullable = false, length = 150)
    private String email;

    @NotBlank
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone must be 10 to 15 digits")
    @Column(name = "teacher_phone", nullable = false, length = 20)
    private String phone;
}
