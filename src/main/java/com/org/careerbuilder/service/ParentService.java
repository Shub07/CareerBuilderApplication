package com.org.careerbuilder.service;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.Parent;

import com.org.careerbuilder.repository.ParentRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ParentService {

    private static final Logger log = LoggerFactory.getLogger(ParentService.class);
    private final ParentRepository repo;

    public ParentService(ParentRepository repo) {
        this.repo = repo;
    }

    public Parent create(Parent parent) {
        log.info("Creating parent: parentId={}, studentId={}", parent.getParentId(), parent.getStudentId());
        return repo.save(parent);
    }

    public Page<Parent> getAll(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        log.info("Fetching parents page={}, size={}, sortBy={}", page, size, sortBy);
        return repo.findAll(pageable);
    }

    public Parent getById(Long id) {
        log.info("Fetching parent by id={}", id);
        return repo.findById(id)
                .orElseThrow(() -> {
                    log.error("Parent not found id={}", id);
                    return new ResourceNotFoundException("Parent not found with id: " + id);
                });
    }

    public Parent getByParentId(String parentId) {
        log.info("Fetching parent by parentId={}", parentId);
        return repo.findByParentId(parentId)
                .orElseThrow(() -> {
                    log.error("Parent not found parentId={}", parentId);
                    return new ResourceNotFoundException("Parent not found with parentId: " + parentId);
                });
    }

    public Parent update(Long id, Parent updated) {
        log.info("Updating parent id={}", id);
        Parent existing = getById(id);
        // Note: parentId (id field) should not be updated as it's the primary key
        // Note: studentId and schoolId are relationships, use setStudent() and setSchool() instead
        existing.setStudent(updated.getStudent());
        existing.setSchool(updated.getSchool());
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
