package com.org.careerbuilder.service;

import com.org.careerbuilder.models.MyClass;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.models.Subject;
import com.org.careerbuilder.repository.MyClassRepository;
import com.org.careerbuilder.repository.StudentRepository;
import com.org.careerbuilder.repository.SubjectRepository;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@Slf4j
public class MyClassServiceImpl implements MyClassService {
    private final MyClassRepository myClassRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    @Autowired
    public MyClassServiceImpl(MyClassRepository myClassRepository,
                              StudentRepository studentRepository,
                              SubjectRepository subjectRepository) {
        this.myClassRepository = myClassRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public MyClass addMyClass(MyClass myClass) {
        // If IDs are provided but entities are not, resolve them
        if (myClass.getStudent() == null && myClass.getStudentId() != null) {
            Student student = studentRepository.findById(myClass.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + myClass.getStudentId()));
            myClass.setStudent(student);
        }

        if (myClass.getSubject() == null && myClass.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(myClass.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + myClass.getSubjectId()));
            myClass.setSubject(subject);
        }

        // Validate that we have both entities
        if (myClass.getStudent() == null) {
            throw new IllegalArgumentException("Student is required");
        }
        if (myClass.getSubject() == null) {
            throw new IllegalArgumentException("Subject is required");
        }

        log.info("Adding MyClass for Student ID: {}, Subject ID: {}",
                myClass.getStudent().getId(), myClass.getSubject().getId());
        return myClassRepository.save(myClass);
    }

    @Override
    public List<MyClass> getMyClassesForStudent(Long studentId, String className, String section) {
        return myClassRepository.findByStudentIdAndClassNameAndSection(studentId, className, section);
    }

    @Override
    public List<MyClass> getMyClassesForStudent(Long studentId) {
        return myClassRepository.findByStudentId(studentId);
    }

    @Override
    public MyClass getMyClassById(Long id) {
        log.info("Fetching MyClass with ID: {}", id);
        return myClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MyClass not found with id: " + id));
    }

    @Override
    public MyClass updateMyClass(Long id, MyClass myClassData) {
        log.info("Updating MyClass with ID: {}", id);
        MyClass myClass = myClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MyClass not found with id: " + id));

        // Update only the fields that are provided
        if (myClassData.getClassName() != null && !myClassData.getClassName().isEmpty()) {
            myClass.setClassName(myClassData.getClassName());
        }
        if (myClassData.getSection() != null && !myClassData.getSection().isEmpty()) {
            myClass.setSection(myClassData.getSection());
        }

        return myClassRepository.save(myClass);
    }

    @Override
    public void deleteMyClass(Long id) {
        log.info("Deleting MyClass with ID: {}", id);
        if (!myClassRepository.existsById(id)) {
            throw new ResourceNotFoundException("MyClass not found with id: " + id);
        }
        myClassRepository.deleteById(id);
        log.info("MyClass with ID: {} deleted successfully", id);
    }
}

