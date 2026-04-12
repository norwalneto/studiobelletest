package com.nwltecnologia.studiobelle.tenant;

import com.nwltecnologia.studiobelle.tenantmaster.Tenant;
import com.nwltecnologia.studiobelle.tenantmaster.TenantRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class TenantLoader {

    private final TenantRepository tenantRepository;

    private final TenantRoutingDataSource routingDataSource;

    public TenantLoader(TenantRepository tenantRepository, TenantRoutingDataSource routingDataSource) {
        this.tenantRepository = tenantRepository;
        this.routingDataSource = routingDataSource;
    }

    @PostConstruct
    public void loadTenants() {

        var tenants = tenantRepository.findAll();

        for (var tenant : tenants) {
            DataSource ds = createDataSource(tenant);
            routingDataSource.addTenant(tenant.getTenantId(), ds);
        }
    }

    private DataSource createDataSource(Tenant tenant) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl("jdbc:postgresql://localhost:5432/" + tenant.getDatabaseName());
        ds.setUsername(tenant.getUsername());
        ds.setPassword(tenant.getPassword());
        ds.setDriverClassName("org.postgresql.Driver");
        return ds;
    }
}
