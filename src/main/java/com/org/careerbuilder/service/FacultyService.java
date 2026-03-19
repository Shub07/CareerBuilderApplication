package com.org.careerbuilder.service;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.Faculty;
import com.org.careerbuilder.repository.FacultyRepository;

@Service
public class FacultyService {
	
	 private static final Logger log = LoggerFactory.getLogger(FacultyService.class);
    
    private final FacultyRepository repo;

    public FacultyService(FacultyRepository repo) {
        this.repo = repo;
    }

    public Faculty create(Faculty faculty) {
        log.info("Creating faculty: {} {}", faculty.getFirstName(), faculty.getLastName());
        return repo.save(faculty);
    }

    public Page<Faculty> getAll(Pageable pageable) {
        log.info("Fetching faculty list with page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return repo.findAll(pageable);
    }

    public Faculty getById(Long id) {
        log.info("Fetching faculty by id: {}", id);

        return repo.findById(id)
                .orElseThrow(() -> {
                    log.error("Faculty not found with ID {}", id);
                    return new ResourceNotFoundException("Faculty not found: " + id);
                });
    }

    public Faculty update(Long id, Faculty updated) {
        log.info("Updating faculty: {}", id);

        Faculty existing = getById(id);
        updated.setId(existing.getId());
        return repo.save(updated);
    }

    public void delete(Long id) {
        log.warn("Deleting faculty: {}", id);

        if (!repo.existsById(id)) {
            log.error("Delete failed, faculty does not exist: {}", id);
            throw new ResourceNotFoundException("Faculty not found: " + id);
        }

        repo.deleteById(id);
        log.info("Faculty deleted successfully with id {}", id);
    }
}
