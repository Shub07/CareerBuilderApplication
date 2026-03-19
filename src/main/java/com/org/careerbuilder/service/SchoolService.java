package com.org.careerbuilder.service;

import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import com.org.careerbuilder.models.School;
import com.org.careerbuilder.repository.SchoolRepository;

import java.util.List;

@Service
public class SchoolService {

    private final SchoolRepository repository;

    public SchoolService(SchoolRepository repository) {
        this.repository = repository;
    }

    public List<School> createSchool(List<School> schools) {
        return repository.saveAll(schools);
    }

    public List<School> getAllSchools() {
        return repository.findAll();
    }

    public School getSchoolById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found: " + id));

    }

    public School updateSchool(Long id, School schoolDetails) {
        School school = getSchoolById(id);

        school.setSchoolName(schoolDetails.getSchoolName());
        school.setSchoolCode(schoolDetails.getSchoolCode());
        school.setSchoolType(schoolDetails.getSchoolType());
        school.setBoardAffiliation(schoolDetails.getBoardAffiliation());
        school.setAffiliationNumber(schoolDetails.getAffiliationNumber());
        school.setYearOfEstablishment(schoolDetails.getYearOfEstablishment());
        school.setMediumOfInstruction(schoolDetails.getMediumOfInstruction());
        school.setSchoolCategory(schoolDetails.getSchoolCategory());
        school.setVillageTownCity(schoolDetails.getVillageTownCity());
        school.setDistrict(schoolDetails.getDistrict());
        school.setStateUT(schoolDetails.getStateUT());

        return repository.save(school);
    }

    public void deleteSchool(Long id) {
        repository.deleteById(id);
    }
}
