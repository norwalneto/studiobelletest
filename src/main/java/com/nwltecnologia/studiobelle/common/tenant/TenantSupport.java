package com.nwltecnologia.studiobelle.common.tenant;

import com.nwltecnologia.studiobelle.security.ApiSecurityException;
import com.nwltecnologia.studiobelle.tenant.TenantContext;
import org.springframework.stereotype.Component;

@Component
public class TenantSupport {
    public String currentTenant() {
        String tenant = TenantContext.getTenant();
        if (tenant == null || tenant.isBlank()) {
            throw new ApiSecurityException("Tenant não informado");
        }
        return tenant;
    }
}
