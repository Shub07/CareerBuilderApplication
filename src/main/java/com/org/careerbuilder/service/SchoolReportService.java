package com.org.careerbuilder.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.org.careerbuilder.dto.SchoolClassDTO;
import com.org.careerbuilder.repository.StudentRepository;

@Service
public class SchoolReportService {

    private final StudentRepository studentRepository;

    public SchoolReportService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<SchoolClassDTO> fetchSchoolClass() {
        return studentRepository.fetchSchoolClassNative();
    }
}
