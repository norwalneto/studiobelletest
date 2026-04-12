package com.nwltecnologia.studiobelle.automation.service;

import com.nwltecnologia.studiobelle.automation.dto.TenantSettingsRequest;
import com.nwltecnologia.studiobelle.automation.entity.TenantSettings;
import com.nwltecnologia.studiobelle.automation.repository.TenantSettingsRepository;
import com.nwltecnologia.studiobelle.common.tenant.TenantSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantSettingsService {
    private final TenantSettingsRepository repository;
    private final TenantSupport tenantSupport;

    public TenantSettingsService(TenantSettingsRepository repository, TenantSupport tenantSupport) {
        this.repository = repository;
        this.tenantSupport = tenantSupport;
    }

    public TenantSettings getOrCreateCurrent() {
        String tenant = tenantSupport.currentTenant();
        return repository.findByTenantId(tenant)
                .orElseGet(() -> repository.save(TenantSettings.builder().tenantId(tenant).inactivityDays(30).aiEnabled(false).automationEnabled(true).build()));
    }

    @Transactional
    public TenantSettings upsert(TenantSettingsRequest request) {
        TenantSettings settings = getOrCreateCurrent();
        settings.setInactivityDays(request.inactivityDays());
        settings.setAiEnabled(request.aiEnabled());
        settings.setAutomationEnabled(request.automationEnabled());
        return repository.save(settings);
    }
}
