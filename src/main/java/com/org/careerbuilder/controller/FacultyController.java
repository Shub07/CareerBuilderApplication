
package com.org.careerbuilder.controller;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.org.careerbuilder.dto.SchoolClassDTO;
import com.org.careerbuilder.models.Faculty;
import com.org.careerbuilder.repository.StudentRepository;
import com.org.careerbuilder.service.FacultyService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/faculty")
 class FacultyController {


//    private static final Logger log = LoggerFactory.getLogger(FacultyController.class);
    
	private final FacultyService service;
	
	@Autowired
	StudentRepository sservice;

	public FacultyController(FacultyService service) {
		this.service = service;
	}
	    
	 @GetMapping("fetch/join/data")
	    public List<SchoolClassDTO> getById_2() {
	        log.info("API: Fetch faculty by id {}");
	        return sservice.fetchSchoolClassNative();
	    }
	
    @PostMapping
    public Faculty create(@Valid @RequestBody Faculty faculty) {
        log.info("API: Creating faculty {}", faculty.getFirstName());
        return service.create(faculty);
    }

    @GetMapping
    public Page<Faculty> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        log.info("API: Fetching faculty list");
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return service.getAll(pageable);
    }

    @GetMapping("/{id}")
    public Faculty getById(@PathVariable Long id) {
        log.info("API: Fetch faculty by id {}", id);
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Faculty update(@PathVariable Long id, @Valid @RequestBody Faculty faculty) {
        log.info("API: Updating faculty {}", id);
        return service.update(id, faculty);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        log.info("API: Deleting faculty {}", id);
        service.delete(id);
        return "Faculty deleted successfully!";
    }
}
