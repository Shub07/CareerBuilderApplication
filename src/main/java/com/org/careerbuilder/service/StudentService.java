package com.org.careerbuilder.service;

import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final StudentRepository repo;
    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    public StudentService(StudentRepository repo) {
        this.repo = repo;
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
}