package com.nwltecnologia.studiobelle.config;

import com.nwltecnologia.studiobelle.tenant.TenantFilter;
import com.nwltecnologia.studiobelle.tenant.TenantRoutingDataSource;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    public FilterRegistrationBean<TenantFilter> tenantFilter() {
        FilterRegistrationBean<TenantFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TenantFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    @Primary
    public DataSource dataSource(TenantRoutingDataSource routingDataSource) {

        // 🔥 BANCO MASTER (onde fica tabela de tenants)
        DriverManagerDataSource defaultDataSource = new DriverManagerDataSource();
        defaultDataSource.setUrl("jdbc:postgresql://localhost:5432/studiobelle");
        defaultDataSource.setUsername("postgres");
        defaultDataSource.setPassword("postgres");
        defaultDataSource.setDriverClassName("org.postgresql.Driver");

        // 🔥 DEFINE O DEFAULT
        routingDataSource.setDefaultTargetDataSource(defaultDataSource);

        return routingDataSource;
    }
}
