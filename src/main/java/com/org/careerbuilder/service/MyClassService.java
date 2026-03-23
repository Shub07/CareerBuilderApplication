package com.org.careerbuilder.service;

import com.org.careerbuilder.models.MyClass;
import java.util.List;

public interface MyClassService {
    // Create
    MyClass addMyClass(MyClass myClass);
    
    // Read
    List<MyClass> getMyClassesForStudent(Long studentId, String className, String section);
    List<MyClass> getMyClassesForStudent(Long studentId);
    MyClass getMyClassById(Long id);
    
    // Update
    MyClass updateMyClass(Long id, MyClass myClassData);
    
    // Delete
    void deleteMyClass(Long id);
}

