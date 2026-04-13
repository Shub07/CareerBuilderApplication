package com.org.careerbuilder.service;

import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.School;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.repository.SchoolRepository;
import com.org.careerbuilder.repository.StudentRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private static final String INDEPENDENT_SCHOOL_CODE = "INDEPENDENT_LEARNERS";

    private final StudentRepository repo;
    private final SchoolRepository schoolRepository;
    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    public StudentService(StudentRepository repo, SchoolRepository schoolRepository) {
        this.repo = repo;
        this.schoolRepository = schoolRepository;
    }

    public Student create(Student student) {
        log.info("Creating student: {}", student.getFirstName());
        return repo.save(student);
    }

    public Page<Student> getAll(Pageable pageable) {
        log.info("Fetching students with pagination {}", pageable);
        return repo.findAll(pageable);
    }

    public Student getById(Long id) {
        log.info("Fetching student by ID: {}", id);

        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    public Student update(Long id, Student updated) {
        Student existing = getById(id);

        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setAge(updated.getAge());
        existing.setClassName(updated.getClassName());
        existing.setRollNo(updated.getRollNo());
        existing.setParentName(updated.getParentName());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setAddress(updated.getAddress());
        existing.setSchool(updated.getSchool());
        existing.setSection(updated.getSection());

        return repo.save(existing);
    }


    public void delete(Long id) {
        log.warn("Deleting student with ID: {}", id);

        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Student not found: " + id);
        }
        repo.deleteById(id);
    }

    public Map<String, Object> resolveSchoolByContact(String email, String mobile) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedMobile = normalizeMobile(mobile);

        if ((normalizedEmail == null || normalizedEmail.isBlank()) && (normalizedMobile == null || normalizedMobile.isBlank())) {
            throw new IllegalArgumentException("Either email or mobile is required.");
        }

        Student existingStudent = null;

        if (normalizedEmail != null) {
            existingStudent = repo.findFirstByEmailIgnoreCase(normalizedEmail).orElse(null);
        }

        if (existingStudent == null && normalizedMobile != null) {
            existingStudent = repo.findFirstByPhone(normalizedMobile).orElse(null);
        }

        Map<String, Object> response = new LinkedHashMap<>();

        if (existingStudent != null && existingStudent.getSchool() != null) {
            response.put("resolved", true);
            response.put("source", "EXISTING_STUDENT");
            response.put("studentId", existingStudent.getId());
            response.put("schoolId", existingStudent.getSchool().getId());
            response.put("schoolName", existingStudent.getSchool().getSchoolName());
            return response;
        }

        School fallbackSchool = getOrCreateIndependentSchool();

        response.put("resolved", true);
        response.put("source", "INDEPENDENT_DEFAULT");
        response.put("studentId", null);
        response.put("schoolId", fallbackSchool.getId());
        response.put("schoolName", fallbackSchool.getSchoolName());
        return response;
    }

    private School getOrCreateIndependentSchool() {
        return schoolRepository.findBySchoolCodeIgnoreCase(INDEPENDENT_SCHOOL_CODE)
                .orElseGet(() -> schoolRepository.save(School.builder()
                        .schoolName("Independent Learners")
                        .schoolCode(INDEPENDENT_SCHOOL_CODE)
                        .schoolType("INDIVIDUAL")
                        .boardAffiliation("SELF_LEARNING")
                        .affiliationNumber("IND-0001")
                        .yearOfEstablishment("2026")
                        .mediumOfInstruction("ENGLISH")
                        .schoolCategory("INDEPENDENT")
                        .villageTownCity("Online")
                        .district("Online")
                        .stateUT("Online")
                        .build()));
    }

    private String normalizeEmail(String value) {
        if (value == null) return null;
        String normalized = value.trim().toLowerCase();
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeMobile(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }
}