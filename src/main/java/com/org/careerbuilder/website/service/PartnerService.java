package com.org.careerbuilder.website.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.org.careerbuilder.website.models.Partner;
import com.org.careerbuilder.website.repository.PartnerRepository;

@Service
public class PartnerService {

    private final PartnerRepository repository;

    public PartnerService(PartnerRepository repository) {
        this.repository = repository;
    }

    public List<Partner> listActivePartners() {
        return repository.findAllByActiveTrueOrderByNameAsc();
    }
}
