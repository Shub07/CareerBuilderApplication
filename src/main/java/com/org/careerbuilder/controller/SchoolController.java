package com.org.careerbuilder.controller;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.org.careerbuilder.models.School;
import com.org.careerbuilder.service.SchoolService;

@RestController
@RequestMapping("/api/schools")
public class SchoolController {

    private final SchoolService service;

    public SchoolController(SchoolService service) {
        this.service = service;
    }

    @PostMapping
    public List<School>  createSchool(@RequestBody List<School> schools) {
        return service.createSchool(schools);
    }

    @GetMapping
    public List<School> getAllSchools() {
        return service.getAllSchools();
    }

    @GetMapping("/{id}")
    public School getSchoolById(@PathVariable Long id) {
        return service.getSchoolById(id);
    }

    @PutMapping("/{id}")
    public School updateSchool(@PathVariable Long id, @RequestBody School schoolDetails) {
        return service.updateSchool(id, schoolDetails);
    }

    @DeleteMapping("/{id}")
    public String deleteSchool(@PathVariable Long id) {
        service.deleteSchool(id);
        return "School deleted successfully";
    }
}
