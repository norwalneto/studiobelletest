package com.nwltecnologia.studiobelle.tenant;

import com.nwltecnologia.studiobelle.tenantmaster.master.MasterTenantDirectory;
import org.springframework.stereotype.Component;

@Component
public class TenantResolver {

    private final MasterTenantDirectory masterTenantDirectory;

    public TenantResolver(MasterTenantDirectory masterTenantDirectory) {
        this.masterTenantDirectory = masterTenantDirectory;
    }

    public String resolveBySubdomain(String subdomain) {
        if (subdomain == null || subdomain.isBlank()) {
            return null;
        }
        return masterTenantDirectory.findBySubdomain(subdomain)
                .map(config -> config.tenantId())
                .orElse(null);
    }

    public String resolveByPhoneNumberId(String phoneNumberId) {
        if (phoneNumberId == null || phoneNumberId.isBlank()) {
            return null;
        }
        return masterTenantDirectory.findWhatsAppByPhoneNumberId(phoneNumberId)
                .map(config -> config.tenantId())
                .orElse(null);
    }
}
