package com.org.careerbuilder.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "schools")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String schoolName;
    private String schoolCode;
    private String schoolType;
    private String boardAffiliation;
    private String affiliationNumber;
    private String yearOfEstablishment;
    private String mediumOfInstruction;
    private String schoolCategory;
    private String villageTownCity;
    private String district;
    private String stateUT;
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getSchoolName() {
		return schoolName;
	}
	public void setSchoolName(String schoolName) {
		this.schoolName = schoolName;
	}
	public String getSchoolCode() {
		return schoolCode;
	}
	public void setSchoolCode(String schoolCode) {
		this.schoolCode = schoolCode;
	}
	public String getSchoolType() {
		return schoolType;
	}
	public void setSchoolType(String schoolType) {
		this.schoolType = schoolType;
	}
	public String getBoardAffiliation() {
		return boardAffiliation;
	}
	public void setBoardAffiliation(String boardAffiliation) {
		this.boardAffiliation = boardAffiliation;
	}
	public String getAffiliationNumber() {
		return affiliationNumber;
	}
	public void setAffiliationNumber(String affiliationNumber) {
		this.affiliationNumber = affiliationNumber;
	}
	public String getYearOfEstablishment() {
		return yearOfEstablishment;
	}
	public void setYearOfEstablishment(String yearOfEstablishment) {
		this.yearOfEstablishment = yearOfEstablishment;
	}
	public String getMediumOfInstruction() {
		return mediumOfInstruction;
	}
	public void setMediumOfInstruction(String mediumOfInstruction) {
		this.mediumOfInstruction = mediumOfInstruction;
	}
	public String getSchoolCategory() {
		return schoolCategory;
	}
	public void setSchoolCategory(String schoolCategory) {
		this.schoolCategory = schoolCategory;
	}
	public String getVillageTownCity() {
		return villageTownCity;
	}
	public void setVillageTownCity(String villageTownCity) {
		this.villageTownCity = villageTownCity;
	}
	public String getDistrict() {
		return district;
	}
	public void setDistrict(String district) {
		this.district = district;
	}
	public String getStateUT() {
		return stateUT;
	}
	public void setStateUT(String stateUT) {
		this.stateUT = stateUT;
	}
	@Override
	public String toString() {
		return "School [id=" + id + ", schoolName=" + schoolName + ", schoolCode=" + schoolCode + ", schoolType="
				+ schoolType + ", boardAffiliation=" + boardAffiliation + ", affiliationNumber=" + affiliationNumber
				+ ", yearOfEstablishment=" + yearOfEstablishment + ", mediumOfInstruction=" + mediumOfInstruction
				+ ", schoolCategory=" + schoolCategory + ", villageTownCity=" + villageTownCity + ", district="
				+ district + ", stateUT=" + stateUT + "]";
	}
//	public School() {
//		super();
//		// TODO Auto-generated constructor stub
	}
    
    


