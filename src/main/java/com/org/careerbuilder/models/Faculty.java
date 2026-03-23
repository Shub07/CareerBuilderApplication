package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(
		name = "faculty",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_faculty_code", columnNames = "faculty_code"),
				@UniqueConstraint(name = "uk_faculty_email", columnNames = "email"),
				@UniqueConstraint(name = "uk_faculty_phone", columnNames = "phone")
		},
		indexes = {
				@Index(name = "idx_faculty_school", columnList = "school_id"),
				@Index(name = "idx_faculty_subject", columnList = "subject_id")
		}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"school", "subject"})
public class Faculty {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "faculty_pk", nullable = false, updatable = false)
	private Long id;

	/**
	 * Business ID for faculty (example: FAC-1001).
	 * Useful for external references and UI.
	 */
	@NotBlank(message = "Faculty ID is required")
	@Size(min = 2, max = 50, message = "Faculty ID must be between 2 and 50 characters")
	@Column(name = "faculty_code", nullable = false, length = 50)
	private String facultyId;

	@NotBlank(message = "First name is required")
	@Size(min = 2, max = 50)
	@Column(name = "first_name", nullable = false, length = 50)
	private String firstName;

	@NotBlank(message = "Last name is required")
	@Size(min = 2, max = 50)
	@Column(name = "last_name", nullable = false, length = 50)
	private String lastName;

	/**
	 * If you want strict allowed values, replace String with an enum.
	 */
	@NotBlank(message = "Gender is required")
	@Size(max = 20)
	@Column(name = "gender", nullable = false, length = 20)
	private String gender;

	@NotNull(message = "Age is required")
	@Min(value = 18, message = "Age must be >= 18")
	@Max(value = 80, message = "Age must be <= 80")
	@Column(name = "age", nullable = false)
	private Integer age;

	/**
	 * Link faculty to a Subject entity instead of storing subject name as String.
	 * This prevents spelling mismatches and supports subject filtering.
	 */
	@NotNull(message = "Subject is required")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "subject_id", nullable = false)
	private Subject subject;

	@NotBlank(message = "Qualification is required")
	@Size(min = 2, max = 100)
	@Column(name = "qualification", nullable = false, length = 100)
	private String qualification;

	@NotNull(message = "Experience is required")
	@Min(value = 0, message = "Experience must be >= 0")
	@Max(value = 60, message = "Experience must be <= 60")
	@Column(name = "experience_years", nullable = false)
	private Integer experience;

	@NotBlank(message = "Phone is required")
	@Pattern(regexp = "^[0-9]{10,15}$", message = "Phone must be 10 to 15 digits")
	@Column(name = "phone", nullable = false, length = 15)
	private String phone;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	@Size(max = 150)
	@Column(name = "email", nullable = false, length = 150)
	private String email;

	@NotBlank(message = "Address is required")
	@Size(min = 5, max = 300)
	@Column(name = "address", nullable = false, length = 300)
	private String address;

	/**
	 * Link faculty to School entity instead of duplicating schoolId/schoolName.
	 */
	@NotNull(message = "School is required")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "school_id", nullable = false)
	private School school;

	// Explicit getters and setters
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	
	public String getFacultyId() { return facultyId; }
	public void setFacultyId(String facultyId) { this.facultyId = facultyId; }
	
	public String getFirstName() { return firstName; }
	public void setFirstName(String firstName) { this.firstName = firstName; }
	
	public String getLastName() { return lastName; }
	public void setLastName(String lastName) { this.lastName = lastName; }
	
	public String getGender() { return gender; }
	public void setGender(String gender) { this.gender = gender; }
	
	public Integer getAge() { return age; }
	public void setAge(Integer age) { this.age = age; }
	
	public Subject getSubject() { return subject; }
	public void setSubject(Subject subject) { this.subject = subject; }
	
	public String getQualification() { return qualification; }
	public void setQualification(String qualification) { this.qualification = qualification; }
	
	public Integer getExperience() { return experience; }
	public void setExperience(Integer experience) { this.experience = experience; }
	
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	
	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }
	
	public School getSchool() { return school; }
	public void setSchool(School school) { this.school = school; }
}
