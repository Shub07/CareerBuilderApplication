package com.org.careerbuilder.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.org.careerbuilder.dto.SchoolClassDTO;
import com.org.careerbuilder.models.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

	@Query(value = """
	SELECT
	  s.school_name AS schoolName,
	  s.school_code AS schoolCode,
	  s.school_type AS schoolType,
	  st.age AS age,
	  st.class_name AS className
	FROM schools s
	JOIN students st ON st.school_id = s.id
	""", nativeQuery = true)
	List<SchoolClassDTO> fetchSchoolClassNative();

}
