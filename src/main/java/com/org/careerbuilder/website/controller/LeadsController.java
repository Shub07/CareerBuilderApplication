package com.org.careerbuilder.website.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.org.careerbuilder.website.dto.DemoRequestCreateDTO;
import com.org.careerbuilder.website.dto.NewsletterSubscribeDTO;
import com.org.careerbuilder.website.dto.PartnerRequestCreateDTO;
import com.org.careerbuilder.website.models.DemoRequest;
import com.org.careerbuilder.website.models.NewsletterSubscriber;
import com.org.careerbuilder.website.models.PartnerRequest;
import com.org.careerbuilder.website.service.LeadService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/leads")
public class LeadsController {

    private final LeadService leadService;

    public LeadsController(LeadService leadService) {
        this.leadService = leadService;
    }

    @PostMapping("/request-demo")
    @ResponseStatus(HttpStatus.CREATED)
    public DemoRequest requestDemo(@Valid @RequestBody DemoRequestCreateDTO dto) {
        log.info("API: Request demo email={}", dto.getEmail());
        DemoRequest request = new DemoRequest();
        request.setName(dto.getName());
        request.setEmail(dto.getEmail());
        request.setPhone(dto.getPhone());
        request.setOrg(dto.getOrg());
        request.setMessage(dto.getMessage());
        return leadService.createDemoRequest(request);
    }

    @PostMapping("/become-partner")
    @ResponseStatus(HttpStatus.CREATED)
    public PartnerRequest becomePartner(@Valid @RequestBody PartnerRequestCreateDTO dto) {
        log.info("API: Become partner org={}", dto.getOrganizationName());
        PartnerRequest request = new PartnerRequest();
        request.setOrganizationName(dto.getOrganizationName());
        request.setContactName(dto.getContactName());
        request.setEmail(dto.getEmail());
        request.setPhone(dto.getPhone());
        request.setMessage(dto.getMessage());
        return leadService.createPartnerRequest(request);
    }

    @PostMapping("/newsletter")
    @ResponseStatus(HttpStatus.CREATED)
    public NewsletterSubscriber subscribeNewsletter(@Valid @RequestBody NewsletterSubscribeDTO dto) {
        log.info("API: Newsletter subscribe email={}", dto.getEmail());
        return leadService.subscribe(dto.getEmail());
    }
}
