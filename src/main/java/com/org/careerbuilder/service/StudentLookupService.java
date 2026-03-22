package com.org.careerbuilder.service;

import com.org.careerbuilder.models.Student;

public interface StudentLookupService {
    Student getStudentOrThrow(Long studentId);
}
