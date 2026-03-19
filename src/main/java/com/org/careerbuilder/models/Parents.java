package com.org.careerbuilder.models;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "parents", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"parent_id"})
})
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Parents {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "parentId is required")
    @Column(name = "parent_id", nullable = false, unique = true)
    private String parentId;

    @NotBlank(message = "studentId is required")
    @Column(name = "student_id", nullable = false)
    private String studentId;

    @NotBlank(message = "schoolId is required")
    @Column(name = "school_id", nullable = false)
    private String schoolId;

    @NotBlank(message = "fatherName is required")
    private String fatherName;

    @NotBlank(message = "motherName is required")
    private String motherName;

    @NotBlank(message = "primaryContact is required")
    @Size(min = 7, max = 20)
    private String primaryContact;

    @Size(min = 7, max = 20)
    private String secondaryContact;

    @Email(message = "Invalid email format")
    @NotBlank(message = "parentEmail is required")
    private String parentEmail;

    private String fatherOccupation;

    private String motherOccupation;

    @Min(value = 0, message = "annualIncome must be >= 0")
    private Integer annualIncome;

    @NotBlank(message = "address is required")
    private String address;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getStudentId() {
		return studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public String getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}

	public String getFatherName() {
		return fatherName;
	}

	public void setFatherName(String fatherName) {
		this.fatherName = fatherName;
	}

	public String getMotherName() {
		return motherName;
	}

	public void setMotherName(String motherName) {
		this.motherName = motherName;
	}

	public String getPrimaryContact() {
		return primaryContact;
	}

	public void setPrimaryContact(String primaryContact) {
		this.primaryContact = primaryContact;
	}

	public String getSecondaryContact() {
		return secondaryContact;
	}

	public void setSecondaryContact(String secondaryContact) {
		this.secondaryContact = secondaryContact;
	}

	public String getParentEmail() {
		return parentEmail;
	}

	public void setParentEmail(String parentEmail) {
		this.parentEmail = parentEmail;
	}

	public String getFatherOccupation() {
		return fatherOccupation;
	}

	public void setFatherOccupation(String fatherOccupation) {
		this.fatherOccupation = fatherOccupation;
	}

	public String getMotherOccupation() {
		return motherOccupation;
	}

	public void setMotherOccupation(String motherOccupation) {
		this.motherOccupation = motherOccupation;
	}

	public Integer getAnnualIncome() {
		return annualIncome;
	}

	public void setAnnualIncome(Integer annualIncome) {
		this.annualIncome = annualIncome;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

//	public Parents() {
//		super();
//		// TODO Auto-generated constructor stub
//	}
    
    
}

