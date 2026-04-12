package com.nwltecnologia.studiobelle.tenant;

import com.nwltecnologia.studiobelle.tenantmaster.Tenant;
import com.nwltecnologia.studiobelle.tenantmaster.TenantRepository;
import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    private final Map<Object, Object> dataSources = new HashMap<>();

    @PostConstruct
    public void init() {
        // 🔥 GARANTE QUE NÃO FIQUE NULL
        super.setTargetDataSources(dataSources);
        super.afterPropertiesSet();
    }


    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getTenant();
    }

    public void addTenant(String tenantId, DataSource dataSource) {
        dataSources.put(tenantId, dataSource);
        super.setTargetDataSources(dataSources);
        super.afterPropertiesSet();
    }
}
