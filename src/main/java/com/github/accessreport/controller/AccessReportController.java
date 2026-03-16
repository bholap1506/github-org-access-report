package com.github.accessreport.controller;

import com.github.accessreport.model.dto.AccessReportDTO;
import com.github.accessreport.service.AccessReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/org")
public class AccessReportController {

    private final AccessReportService accessReportService;

    public AccessReportController(AccessReportService accessReportService) {
        this.accessReportService = accessReportService;
    }

    @GetMapping("/{org}/access-report")
    public ResponseEntity<AccessReportDTO> getAccessReport(@PathVariable String org) {
        AccessReportDTO report = accessReportService.generateAccessReport(org);
        return ResponseEntity.ok(report);
    }
}
