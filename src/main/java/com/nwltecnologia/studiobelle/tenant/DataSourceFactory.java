package com.nwltecnologia.studiobelle.tenant;

import com.nwltecnologia.studiobelle.tenantmaster.Tenant;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DataSourceFactory {

    public DataSource createDataSource(Tenant tenant) {

        DriverManagerDataSource ds = new DriverManagerDataSource();

        ds.setUrl("jdbc:postgresql://localhost:5432/" + tenant.getDatabaseName());
        ds.setUsername(tenant.getUsername());
        ds.setPassword(tenant.getPassword());
        ds.setDriverClassName("org.postgresql.Driver");

        return ds;
    }
}
