package com.org.careerbuilder.service;

import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentLookupServiceImpl implements StudentLookupService {

    private final StudentRepository studentRepository;

    @Override
    public Student getStudentOrThrow(Long studentId) {
        log.info("Fetching student with id={}", studentId);
        return studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.error("Student not found with id={}", studentId);
                    return new ResourceNotFoundException("Student not found with id: " + studentId);
                });
    }
}

