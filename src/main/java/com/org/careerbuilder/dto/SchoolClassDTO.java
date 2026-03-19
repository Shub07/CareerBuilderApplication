package com.org.careerbuilder.dto;

public class SchoolClassDTO {

	  // school 
	    private String schoolName;
	    private String schoolCode;
	    private String schoolType;
	    
	    
	    //student 
	    private Integer age;
	    private String className;
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
		public Integer getAge() {
			return age;
		}
		public void setAge(Integer age) {
			this.age = age;
		}
		public String getClassName() {
			return className;
		}
		public void setClassName(String className) {
			this.className = className;
		}
		public SchoolClassDTO(String schoolName, String schoolCode, String schoolType, Integer age, String className) {
			super();
			this.schoolName = schoolName;
			this.schoolCode = schoolCode;
			this.schoolType = schoolType;
			this.age = age;
			this.className = className;
		}
	    
		
	    
}
