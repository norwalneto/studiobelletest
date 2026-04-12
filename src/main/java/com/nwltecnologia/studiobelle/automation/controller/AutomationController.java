package com.nwltecnologia.studiobelle.automation.controller;

import com.nwltecnologia.studiobelle.automation.dto.AutomationRequest;
import com.nwltecnologia.studiobelle.automation.dto.AutomationResponse;
import com.nwltecnologia.studiobelle.automation.dto.TenantSettingsRequest;
import com.nwltecnologia.studiobelle.automation.entity.TenantSettings;
import com.nwltecnologia.studiobelle.automation.service.AutomationService;
import com.nwltecnologia.studiobelle.automation.service.TenantSettingsService;
import com.nwltecnologia.studiobelle.common.tenant.TenantSupport;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/automations")
public class AutomationController {

    private final AutomationService automationService;
    private final TenantSettingsService tenantSettingsService;
    private final TenantSupport tenantSupport;

    public AutomationController(AutomationService automationService,
                                TenantSettingsService tenantSettingsService,
                                TenantSupport tenantSupport) {
        this.automationService = automationService;
        this.tenantSettingsService = tenantSettingsService;
        this.tenantSupport = tenantSupport;
    }

    @GetMapping
    public ResponseEntity<List<AutomationResponse>> findAll() {
        return ResponseEntity.ok(automationService.findAllByCurrentTenant(tenantSupport.currentTenant()));
    }

    @PostMapping
    public ResponseEntity<AutomationResponse> upsert(@Valid @RequestBody AutomationRequest request) {
        return ResponseEntity.ok(automationService.upsert(tenantSupport.currentTenant(), request));
    }

    @GetMapping("/settings")
    public ResponseEntity<TenantSettings> getSettings() {
        return ResponseEntity.ok(tenantSettingsService.getOrCreateCurrent());
    }

    @PutMapping("/settings")
    public ResponseEntity<TenantSettings> saveSettings(@Valid @RequestBody TenantSettingsRequest request) {
        return ResponseEntity.ok(tenantSettingsService.upsert(request));
    }
}
