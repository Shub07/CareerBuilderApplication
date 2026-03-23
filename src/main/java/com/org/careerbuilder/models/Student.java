package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(
		name = "students",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_student_email", columnNames = "email"),
				@UniqueConstraint(name = "uk_student_phone", columnNames = "phone"),
				@UniqueConstraint(name = "uk_student_roll", columnNames = {"school_id", "class_name", "section", "roll_no"})
		},
		indexes = {
				@Index(name = "idx_student_class_section", columnList = "class_name, section"),
				@Index(name = "idx_student_school", columnList = "school_id")
		}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, updatable = false)
	private Long id;

	@NotBlank @Size(min = 2, max = 50)
	@Column(name = "first_name", nullable = false, length = 50)
	private String firstName;

	@NotBlank @Size(min = 2, max = 50)
	@Column(name = "last_name", nullable = false, length = 50)
	private String lastName;

	@NotNull @Min(3) @Max(100)
	@Column(name = "age", nullable = false)
	private Integer age;

	@NotBlank @Size(max = 50)
	@Column(name = "class_name", nullable = false, length = 50)
	private String className;

	@NotBlank
	@Pattern(regexp = "^[A-Za-z]{1,3}|NA$", message = "Section must be A/B/C or NA")
	@Column(name = "section", nullable = false, length = 10)
	private String section;

	@NotNull @Min(1)
	@Column(name = "roll_no", nullable = false)
	private Integer rollNo;

	@NotBlank @Size(min = 2, max = 100)
	@Column(name = "parent_name", nullable = false, length = 100)
	private String parentName;

	@NotBlank
	@Pattern(regexp = "^[0-9]{10,15}$", message = "Phone must be 10 to 15 digits")
	@Column(name = "phone", nullable = false, length = 15)
	private String phone;

	@NotBlank @Email @Size(max = 150)
	@Column(name = "email", nullable = false, length = 150)
	private String email;

	@NotBlank @Size(min = 5, max = 300)
	@Column(name = "address", nullable = false, length = 300)
	private String address;

	/**
	 * Link student to School entity instead of storing schoolId/schoolName as Strings.
	 * This prevents data inconsistency and supports proper foreign key relationships.
	 */
	@NotNull(message = "School is required")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "school_id", nullable = false)
	private School school;

	// Explicit getters and setters
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	
	public String getFirstName() { return firstName; }
	public void setFirstName(String firstName) { this.firstName = firstName; }
	
	public String getLastName() { return lastName; }
	public void setLastName(String lastName) { this.lastName = lastName; }
	
	public Integer getAge() { return age; }
	public void setAge(Integer age) { this.age = age; }
	
	public String getClassName() { return className; }
	public void setClassName(String className) { this.className = className; }
	
	public String getSection() { return section; }
	public void setSection(String section) { this.section = section; }
	
	public Integer getRollNo() { return rollNo; }
	public void setRollNo(Integer rollNo) { this.rollNo = rollNo; }
	
	public String getParentName() { return parentName; }
	public void setParentName(String parentName) { this.parentName = parentName; }
	
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	
	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }
	
	public School getSchool() { return school; }
	public void setSchool(School school) { this.school = school; }
}
