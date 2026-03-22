package com.org.careerbuilder.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.org.careerbuilder.dto.SchoolClassDTO;
import com.org.careerbuilder.service.SchoolReportService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    private final SchoolReportService schoolReportService;

    public ReportsController(SchoolReportService schoolReportService) {
        this.schoolReportService = schoolReportService;
    }

    @GetMapping("/school-class")
    public List<SchoolClassDTO> getSchoolClassReport() {
        log.info("API: Fetch school-class report");
        return schoolReportService.fetchSchoolClass();
    }
}
