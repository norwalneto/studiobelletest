package com.nwltecnologia.studiobelle.company.service;

import com.nwltecnologia.studiobelle.common.tenant.TenantSupport;
import com.nwltecnologia.studiobelle.company.dto.CompanyProfileRequest;
import com.nwltecnologia.studiobelle.company.dto.CompanyProfileResponse;
import com.nwltecnologia.studiobelle.company.entity.CompanyProfile;
import com.nwltecnologia.studiobelle.company.repository.CompanyProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyProfileService {

    private final CompanyProfileRepository repository;
    private final TenantSupport tenantSupport;

    public CompanyProfileService(CompanyProfileRepository repository, TenantSupport tenantSupport) {
        this.repository = repository;
        this.tenantSupport = tenantSupport;
    }

    public CompanyProfileResponse getCurrent() {
        String tenant = tenantSupport.currentTenant();
        CompanyProfile profile = repository.findByTenantId(tenant)
                .orElseGet(() -> repository.save(CompanyProfile.builder()
                        .tenantId(tenant)
                        .name("Minha Empresa")
                        .businessHours("SEG-SEX 08:00-18:00")
                        .build()));
        return toResponse(profile);
    }

    @Transactional
    public CompanyProfileResponse upsert(CompanyProfileRequest request) {
        String tenant = tenantSupport.currentTenant();
        CompanyProfile profile = repository.findByTenantId(tenant).orElseGet(CompanyProfile::new);
        profile.setTenantId(tenant);
        profile.setName(request.name());
        profile.setPhone(request.phone());
        profile.setWhatsapp(request.whatsapp());
        profile.setAddress(request.address());
        profile.setBusinessHours(request.businessHours());
        return toResponse(repository.save(profile));
    }

    private CompanyProfileResponse toResponse(CompanyProfile profile) {
        return new CompanyProfileResponse(profile.getId(), profile.getTenantId(), profile.getName(), profile.getPhone(),
                profile.getWhatsapp(), profile.getAddress(), profile.getBusinessHours(), profile.getCreatedAt(), profile.getUpdatedAt());
    }
}
