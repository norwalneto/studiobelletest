package com.nwltecnologia.studiobelle.tenant;

import com.nwltecnologia.studiobelle.tenantmaster.master.MasterTenantDirectory;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class TenantLoader {

    private final MasterTenantDirectory masterTenantDirectory;
    private final TenantRoutingDataSource routingDataSource;
    private final DataSourceFactory dataSourceFactory;

    public TenantLoader(MasterTenantDirectory masterTenantDirectory,
                        TenantRoutingDataSource routingDataSource,
                        DataSourceFactory dataSourceFactory) {
        this.masterTenantDirectory = masterTenantDirectory;
        this.routingDataSource = routingDataSource;
        this.dataSourceFactory = dataSourceFactory;
    }

    @PostConstruct
    public void loadTenants() {
        masterTenantDirectory.findAllTenantDatabases().forEach(tenant ->
                routingDataSource.addTenant(tenant.tenantId(), dataSourceFactory.createDataSource(tenant))
        );
    }
}
