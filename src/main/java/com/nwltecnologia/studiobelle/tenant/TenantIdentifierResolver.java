package com.nwltecnologia.studiobelle.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {
    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenant = TenantContext.getTenant();
        return tenant != null ? tenant : "default";
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
