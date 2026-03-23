package com.org.careerbuilder.controller;

import com.org.careerbuilder.models.MyClass;
import com.org.careerbuilder.service.MyClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/myclasses")
@Slf4j
@CrossOrigin(
    origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class MyClassController {
    private final MyClassService myClassService;

    @Autowired
    public MyClassController(MyClassService myClassService) {
        this.myClassService = myClassService;
    }

    // Create: Add MyClass data for a student
    @PostMapping
    public ResponseEntity<MyClass> addMyClass(@RequestBody MyClass myClass) {
        log.info("Creating new MyClass for Student ID: {}, Subject ID: {}", 
                myClass.getStudentId(), myClass.getSubjectId());
        MyClass saved = myClassService.addMyClass(myClass);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Read: Get all MyClasses for a student (with optional filters)
    @GetMapping
    public ResponseEntity<List<MyClass>> getMyClassesForStudent(
            @RequestParam Long studentId,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section) {
        log.info("Fetching MyClasses for Student ID: {}", studentId);
        List<MyClass> result;
        if (className != null && section != null) {
            result = myClassService.getMyClassesForStudent(studentId, className, section);
        } else {
            result = myClassService.getMyClassesForStudent(studentId);
        }
        return ResponseEntity.ok(result);
    }

    // Read: Get MyClass by ID
    @GetMapping("/{id}")
    public ResponseEntity<MyClass> getMyClassById(@PathVariable Long id) {
        log.info("Fetching MyClass with ID: {}", id);
        MyClass myClass = myClassService.getMyClassById(id);
        return ResponseEntity.ok(myClass);
    }

    // Update: Update existing MyClass
    @PutMapping("/{id}")
    public ResponseEntity<MyClass> updateMyClass(
            @PathVariable Long id,
            @RequestBody MyClass myClassData) {
        log.info("Updating MyClass with ID: {}", id);
        MyClass updated = myClassService.updateMyClass(id, myClassData);
        return ResponseEntity.ok(updated);
    }

    // Delete: Delete MyClass by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMyClass(@PathVariable Long id) {
        log.info("Deleting MyClass with ID: {}", id);
        myClassService.deleteMyClass(id);
        return ResponseEntity.noContent().build();
    }
}

