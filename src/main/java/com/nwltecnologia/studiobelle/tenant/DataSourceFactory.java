package com.nwltecnologia.studiobelle.tenant;

import com.nwltecnologia.studiobelle.tenantmaster.Tenant;
import com.nwltecnologia.studiobelle.tenantmaster.master.TenantDatabaseConfig;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DataSourceFactory {

    public DataSource createDataSource(Tenant tenant) {
        return createDataSource(tenant.getDatabaseName(), tenant.getUsername(), tenant.getPassword());
    }

    public DataSource createDataSource(TenantDatabaseConfig tenant) {
        return createDataSource(tenant.databaseName(), tenant.username(), tenant.password());
    }

    private DataSource createDataSource(String databaseName, String username, String password) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl("jdbc:postgresql://localhost:5432/" + databaseName);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("org.postgresql.Driver");
        return ds;
    }
}
