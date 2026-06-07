package com.org.careerbuilder.repository;

import com.org.careerbuilder.dto.SchoolClassDTO;
import com.org.careerbuilder.models.Student;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

	Optional<Student> findFirstByEmailIgnoreCase(String email);

	Optional<Student> findFirstByPhone(String phone);

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

	List<Student> findBySchool_IdAndClassNameAndSection(Long schoolId, String className, String section);

	long countBySchool_IdAndClassNameAndSection(Long schoolId, String className, String section);

	long countBySchool_IdAndClassName(Long schoolId, String className);

	List<Student> findBySchool_IdAndClassName(Long schoolId, String className);

	Page<Student> findBySchool_IdAndClassName(Long schoolId, String className, Pageable pageable);

	Page<Student> findBySchool_IdAndClassNameAndSection(
			Long schoolId, String className, String section, Pageable pageable);

	Optional<Student> findByIdAndSchool_Id(Long id, Long schoolId);

	/** Candidates for the Add Student dropdown: students not already in the given class. */
	@Query("""
			SELECT s FROM Student s
			WHERE s.school.id = :schoolId
			  AND s.className <> :excludeClass
			  AND (:q IS NULL OR :q = ''
			       OR LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE LOWER(CONCAT('%', :q, '%'))
			       OR EXISTS (SELECT 1 FROM StudentProfile p
			                  WHERE p.student.id = s.id
			                  AND LOWER(p.admissionNumber) LIKE LOWER(CONCAT('%', :q, '%'))))
			ORDER BY s.firstName, s.lastName
			""")
	List<Student> searchAddable(@Param("schoolId") Long schoolId,
	                            @Param("excludeClass") String excludeClass,
	                            @Param("q") String q,
	                            Pageable pageable);

	@Query("""
			SELECT DISTINCT s.className, s.section FROM Student s
			WHERE s.school.id = :schoolId
			ORDER BY s.className, s.section
			""")
	List<Object[]> findDistinctClassSections(@Param("schoolId") Long schoolId);

	@Query("""
			SELECT s FROM Student s
			WHERE s.school.id = :schoolId
			AND (
			  LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE LOWER(CONCAT('%', :q, '%'))
			  OR cast(s.rollNo as string) LIKE concat('%', :q, '%')
			)
			""")
	List<Student> searchBySchoolText(@Param("schoolId") Long schoolId, @Param("q") String q);

	Page<Student> findBySchool_Id(Long schoolId, Pageable pageable);

	long countBySchool_Id(Long schoolId);

	@Query("""
			SELECT MAX(s.rollNo) FROM Student s
			WHERE s.school.id = :schoolId AND s.className = :className AND s.section = :section
			""")
	Integer findMaxRollNo(@Param("schoolId") Long schoolId,
	                      @Param("className") String className,
	                      @Param("section") String section);

	@Query("""
			SELECT s FROM Student s
			WHERE s.school.id = :schoolId
			ORDER BY s.className, s.section, s.rollNo
			""")
	List<Student> findAllBySchool_IdOrderByClass(@Param("schoolId") Long schoolId);

	List<Student> findBySchool_IdAndIdIn(Long schoolId, Collection<Long> ids);
}
