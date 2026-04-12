package com.nwltecnologia.studiobelle.tenantmaster.master;

public record TenantDatabaseConfig(
        String tenantId,
        String subdomain,
        String databaseName,
        String username,
        String password
) {
}
