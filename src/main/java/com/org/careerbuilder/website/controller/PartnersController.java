package com.org.careerbuilder.website.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.org.careerbuilder.website.models.Partner;
import com.org.careerbuilder.website.service.PartnerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/partners")
public class PartnersController {

    private final PartnerService partnerService;

    public PartnersController(PartnerService partnerService) {
        this.partnerService = partnerService;
    }

    @GetMapping
    public List<Partner> listPartners() {
        log.info("API: List partners");
        return partnerService.listActivePartners();
    }
}
