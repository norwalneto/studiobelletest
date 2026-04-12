package com.nwltecnologia.studiobelle.company.controller;

import com.nwltecnologia.studiobelle.company.dto.CompanyProfileRequest;
import com.nwltecnologia.studiobelle.company.dto.CompanyProfileResponse;
import com.nwltecnologia.studiobelle.company.service.CompanyProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company-profile")
public class CompanyProfileController {

    private final CompanyProfileService service;

    public CompanyProfileController(CompanyProfileService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<CompanyProfileResponse> getCurrent() {
        return ResponseEntity.ok(service.getCurrent());
    }

    @PutMapping
    public ResponseEntity<CompanyProfileResponse> upsert(@Valid @RequestBody CompanyProfileRequest request) {
        return ResponseEntity.ok(service.upsert(request));
    }
}
