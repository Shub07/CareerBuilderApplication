package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(
		name = "parents",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_parent_code", columnNames = "parent_code"),
				@UniqueConstraint(name = "uk_parent_email", columnNames = "parent_email")
		},
		indexes = {
				@Index(name = "idx_parent_student", columnList = "student_id"),
				@Index(name = "idx_parent_school", columnList = "school_id")
		}
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString(exclude = {"student", "school"})
public class Parent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "parent_pk", nullable = false, updatable = false)
	private Long id;

	/**
	 * Business identifier for parent (example: PARENT-1001).
	 * Keep unique for external references.
	 */
	@NotBlank(message = "parentCode is required")
	@Size(min = 2, max = 50)
	@Column(name = "parent_code", nullable = false, length = 50)
	private String parentCode;

	/**
	 * Mapping to Student. Use this instead of storing studentId as String.
	 */
	@NotNull(message = "Student is required")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	private Student student;

	/**
	 * Mapping to School. Use this instead of storing schoolId as String.
	 */
	@NotNull(message = "School is required")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "school_id", nullable = false)
	private School school;

	@NotBlank(message = "Father name is required")
	@Size(min = 2, max = 120)
	@Column(name = "father_name", nullable = false, length = 120)
	private String fatherName;

	@NotBlank(message = "Mother name is required")
	@Size(min = 2, max = 120)
	@Column(name = "mother_name", nullable = false, length = 120)
	private String motherName;

	@NotBlank(message = "Primary contact is required")
	@Pattern(regexp = "^[0-9]{7,20}$", message = "Primary contact must be 7 to 20 digits")
	@Column(name = "primary_contact", nullable = false, length = 20)
	private String primaryContact;

	@Pattern(regexp = "^[0-9]{7,20}$", message = "Secondary contact must be 7 to 20 digits")
	@Column(name = "secondary_contact", length = 20)
	private String secondaryContact;

	@NotBlank(message = "Parent email is required")
	@Email(message = "Invalid email format")
	@Size(max = 150)
	@Column(name = "parent_email", nullable = false, length = 150)
	private String parentEmail;

	@Size(max = 100)
	@Column(name = "father_occupation", length = 100)
	private String fatherOccupation;

	@Size(max = 100)
	@Column(name = "mother_occupation", length = 100)
	private String motherOccupation;

	@Min(value = 0, message = "Annual income must be >= 0")
	@Column(name = "annual_income")
	private Integer annualIncome;

	@NotBlank(message = "Address is required")
	@Size(min = 5, max = 300)
	@Column(name = "address", nullable = false, length = 300)
	private String address;

	public Object getStudentId() {
		return student.getId();
	}

	public Object getParentId() {
		return id;
	}
}
