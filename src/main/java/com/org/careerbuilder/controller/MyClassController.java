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

    // ==================== CREATE ENDPOINTS ====================
    
    // Create: Add MyClass data for a student
    @PostMapping
    public ResponseEntity<MyClass> addMyClass(@RequestBody MyClass myClass) {
        try {
            log.info("Creating new MyClass for Student ID: {}, Subject ID: {}", 
                    myClass.getStudentId(), myClass.getSubjectId());
            MyClass saved = myClassService.addMyClass(myClass);
            log.info("MyClass created successfully with ID: {}", saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Error creating MyClass: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ==================== READ ENDPOINTS ====================
    
    // Read: Get ALL MyClasses (no filter)
    @GetMapping("/all")
    public ResponseEntity<List<MyClass>> getAllMyClasses() {
        try {
            log.info("Fetching all MyClasses");
            List<MyClass> result = myClassService.getAllMyClasses();
            log.info("Retrieved {} total MyClasses", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching all MyClasses: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Read: Get MyClasses for a SPECIFIC STUDENT
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<MyClass>> getMyClassesByStudent(@PathVariable Long studentId) {
        try {
            log.info("Fetching MyClasses for Student ID: {}", studentId);
            List<MyClass> result = myClassService.getMyClassesForStudent(studentId);
            log.info("Retrieved {} MyClasses for student {}", result.size(), studentId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching MyClasses for student {}: {}", studentId, e.getMessage(), e);
            throw e;
        }
    }

    // Read: Get MyClasses with FILTERS (className and section)
    @GetMapping("/filter")
    public ResponseEntity<List<MyClass>> getFilteredMyClasses(
            @RequestParam(required = true) Long studentId,
            @RequestParam(required = true) String className,
            @RequestParam(required = true) String section) {
        try {
            log.info("Fetching filtered MyClasses - studentId: {}, className: {}, section: {}", 
                    studentId, className, section);
            List<MyClass> result = myClassService.getMyClassesForStudent(studentId, className, section);
            log.info("Retrieved {} filtered MyClasses", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching filtered MyClasses: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Read: Get MyClass by ID
    @GetMapping("/{id}")
    public ResponseEntity<MyClass> getMyClassById(@PathVariable Long id) {
        try {
            log.info("Fetching MyClass with ID: {}", id);
            MyClass myClass = myClassService.getMyClassById(id);
            log.info("Retrieved MyClass with ID: {}", id);
            return ResponseEntity.ok(myClass);
        } catch (Exception e) {
            log.error("Error fetching MyClass {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // ==================== UPDATE ENDPOINTS ====================
    
    // Update: Update existing MyClass
    @PutMapping("/{id}")
    public ResponseEntity<MyClass> updateMyClass(
            @PathVariable Long id,
            @RequestBody MyClass myClassData) {
        try {
            log.info("Updating MyClass with ID: {}", id);
            MyClass updated = myClassService.updateMyClass(id, myClassData);
            log.info("MyClass updated successfully - ID: {}", id);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating MyClass {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // ==================== DELETE ENDPOINTS ====================
    
    // Delete: Delete MyClass by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMyClass(@PathVariable Long id) {
        try {
            log.info("Deleting MyClass with ID: {}", id);
            myClassService.deleteMyClass(id);
            log.info("MyClass deleted successfully - ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting MyClass {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // Delete: Delete ALL MyClasses for a STUDENT
    @DeleteMapping("/student/{studentId}")
    public ResponseEntity<Void> deleteAllMyClassesForStudent(@PathVariable Long studentId) {
        try {
            log.warn("ADMIN ACTION: Deleting ALL MyClasses for Student ID: {}", studentId);
            List<MyClass> classesToDelete = myClassService.getMyClassesForStudent(studentId);
            for (MyClass myClass : classesToDelete) {
                myClassService.deleteMyClass(myClass.getId());
            }
            log.warn("All {} MyClasses deleted for Student ID: {}", classesToDelete.size(), studentId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting MyClasses for student {}: {}", studentId, e.getMessage(), e);
            throw e;
        }
    }
}

