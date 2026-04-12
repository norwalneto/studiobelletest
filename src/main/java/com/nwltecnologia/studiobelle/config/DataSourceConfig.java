package com.nwltecnologia.studiobelle.config;

import com.nwltecnologia.studiobelle.security.JwtAuthenticationFilter;
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
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter(JwtAuthenticationFilter jwtAuthenticationFilter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtAuthenticationFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(2);
        return registration;
    }

    @Bean
    @Primary
    public DataSource dataSource(TenantRoutingDataSource routingDataSource) {

        DriverManagerDataSource defaultDataSource = new DriverManagerDataSource();
        defaultDataSource.setUrl("jdbc:postgresql://localhost:5432/studiobelle");
        defaultDataSource.setUsername("postgres");
        defaultDataSource.setPassword("postgres");
        defaultDataSource.setDriverClassName("org.postgresql.Driver");

        routingDataSource.setDefaultTargetDataSource(defaultDataSource);

        return routingDataSource;
    }
}
