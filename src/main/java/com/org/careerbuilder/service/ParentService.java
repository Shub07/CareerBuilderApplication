package com.org.careerbuilder.service;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.Parents;
import com.org.careerbuilder.repository.ParentRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ParentService {

//	private static final Logger log = LoggerFactory.getLogger(FacultyService.class);
    private final ParentRepository repo;

    public ParentService(ParentRepository repo) {
        this.repo = repo;
    }

    public Parents create(Parents parent) {
        log.info("Creating parent: parentId={}, studentId={}", parent.getParentId(), parent.getStudentId());
        return repo.save(parent);
    }

    public Page<Parents> getAll(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        log.info("Fetching parents page={}, size={}, sortBy={}", page, size, sortBy);
        return repo.findAll(pageable);
    }

    public Parents getById(Long id) {
        log.info("Fetching parent by id={}", id);
        return repo.findById(id)
                .orElseThrow(() -> {
                    log.error("Parent not found id={}", id);
                    return new ResourceNotFoundException("Parent not found with id: " + id);
                });
    }

    public Parents getByParentId(String parentId) {
        log.info("Fetching parent by parentId={}", parentId);
        return repo.findByParentId(parentId)
                .orElseThrow(() -> {
                    log.error("Parent not found parentId={}", parentId);
                    return new ResourceNotFoundException("Parent not found with parentId: " + parentId);
                });
    }

    public Parents update(Long id, Parents updated) {
        log.info("Updating parent id={}", id);
        Parents existing = getById(id);
        existing.setParentId(updated.getParentId());
        existing.setStudentId(updated.getStudentId());
        existing.setSchoolId(updated.getSchoolId());
        existing.setFatherName(updated.getFatherName());
        existing.setMotherName(updated.getMotherName());
        existing.setPrimaryContact(updated.getPrimaryContact());
        existing.setSecondaryContact(updated.getSecondaryContact());
        existing.setParentEmail(updated.getParentEmail());
        existing.setFatherOccupation(updated.getFatherOccupation());
        existing.setMotherOccupation(updated.getMotherOccupation());
        existing.setAnnualIncome(updated.getAnnualIncome());
        existing.setAddress(updated.getAddress());
        return repo.save(existing);
    }

    public void delete(Long id) {
        log.info("Deleting parent id={}", id);
        if (!repo.existsById(id)) {
            log.error("Delete failed. Parent not found id={}", id);
            throw new ResourceNotFoundException("Parent not found with id: " + id);
        }
        repo.deleteById(id);
        log.info("Parent deleted id={}", id);
    }
}
