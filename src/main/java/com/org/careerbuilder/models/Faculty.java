package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "faculty")
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Faculty ID is required")
    private String facultyId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Gender required")
    private String gender;

    @NotNull(message = "Age is required")
    private Integer age;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Qualification required")
    private String qualification;

    @NotNull(message = "Experience is required")
    private Integer experience;

    @NotBlank(message = "Phone required")
    private String phone;

    @Email(message = "Invalid email format")
    @NotBlank
    private String email;

    @NotBlank(message = "Address required")
    private String address;

    @NotBlank(message = "School ID required")
    private String schoolId;

    @NotBlank(message = "School name required")
    private String schoolName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFacultyId() {
		return facultyId;
	}

	public void setFacultyId(String facultyId) {
		this.facultyId = facultyId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getQualification() {
		return qualification;
	}

	public void setQualification(String qualification) {
		this.qualification = qualification;
	}

	public Integer getExperience() {
		return experience;
	}

	public void setExperience(Integer experience) {
		this.experience = experience;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}

	public String getSchoolName() {
		return schoolName;
	}

	public void setSchoolName(String schoolName) {
		this.schoolName = schoolName;
	}

	public Faculty() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "Faculty [id=" + id + ", facultyId=" + facultyId + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", gender=" + gender + ", age=" + age + ", subject=" + subject + ", qualification=" + qualification
				+ ", experience=" + experience + ", phone=" + phone + ", email=" + email + ", address=" + address
				+ ", schoolId=" + schoolId + ", schoolName=" + schoolName + "]";
	}

    // getters and setters
    
    
}

