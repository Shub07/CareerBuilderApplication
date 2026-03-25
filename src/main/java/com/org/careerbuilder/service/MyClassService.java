package com.org.careerbuilder.service;

import com.org.careerbuilder.models.MyClass;
import java.util.List;

public interface MyClassService {
    // ==================== CREATE ====================
    MyClass addMyClass(MyClass myClass);
    
    // ==================== READ ====================
    List<MyClass> getAllMyClasses();
    List<MyClass> getMyClassesForStudent(Long studentId);
    List<MyClass> getMyClassesForStudent(Long studentId, String className, String section);
    MyClass getMyClassById(Long id);
    
    // ==================== UPDATE ====================
    MyClass updateMyClass(Long id, MyClass myClassData);
    
    // ==================== DELETE ====================
    void deleteMyClass(Long id);
    void deleteAllMyClasses();
}

