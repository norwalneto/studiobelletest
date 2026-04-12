package com.nwltecnologia.studiobelle.tenantmaster.master;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Repository
public class MasterTenantDirectory {

    private final JdbcTemplate jdbcTemplate;

    public MasterTenantDirectory(@Qualifier("masterDataSource") DataSource masterDataSource) {
        this.jdbcTemplate = new JdbcTemplate(masterDataSource);
    }

    public List<TenantDatabaseConfig> findAllTenantDatabases() {
        return jdbcTemplate.query("""
                        SELECT tenant_id, subdomain, database_name, username, password
                        FROM tenants
                        WHERE tenant_id IS NOT NULL
                        """,
                (rs, rowNum) -> new TenantDatabaseConfig(
                        rs.getString("tenant_id"),
                        rs.getString("subdomain"),
                        rs.getString("database_name"),
                        rs.getString("username"),
                        rs.getString("password")
                ));
    }

    public Optional<TenantDatabaseConfig> findBySubdomain(String subdomain) {
        List<TenantDatabaseConfig> rows = jdbcTemplate.query("""
                        SELECT tenant_id, subdomain, database_name, username, password
                        FROM tenants
                        WHERE lower(subdomain) = lower(?)
                        LIMIT 1
                        """,
                (rs, rowNum) -> new TenantDatabaseConfig(
                        rs.getString("tenant_id"),
                        rs.getString("subdomain"),
                        rs.getString("database_name"),
                        rs.getString("username"),
                        rs.getString("password")
                ),
                subdomain
        );
        return rows.stream().findFirst();
    }

    public Optional<TenantDatabaseConfig> findByTenantId(String tenantId) {
        List<TenantDatabaseConfig> rows = jdbcTemplate.query("""
                        SELECT tenant_id, subdomain, database_name, username, password
                        FROM tenants
                        WHERE tenant_id = ?
                        LIMIT 1
                        """,
                (rs, rowNum) -> new TenantDatabaseConfig(
                        rs.getString("tenant_id"),
                        rs.getString("subdomain"),
                        rs.getString("database_name"),
                        rs.getString("username"),
                        rs.getString("password")
                ),
                tenantId
        );
        return rows.stream().findFirst();
    }

    public Optional<WhatsAppAccountConfig> findWhatsAppByPhoneNumberId(String phoneNumberId) {
        List<WhatsAppAccountConfig> rows = jdbcTemplate.query("""
                        SELECT tenant_id, phone_number_id, business_account_id, access_token
                        FROM whatsapp_accounts
                        WHERE phone_number_id = ?
                        LIMIT 1
                        """,
                (rs, rowNum) -> new WhatsAppAccountConfig(
                        rs.getString("tenant_id"),
                        rs.getString("phone_number_id"),
                        rs.getString("business_account_id"),
                        rs.getString("access_token")
                ),
                phoneNumberId
        );
        return rows.stream().findFirst();
    }

    public Optional<WhatsAppAccountConfig> findWhatsAppByTenantId(String tenantId) {
        List<WhatsAppAccountConfig> rows = jdbcTemplate.query("""
                        SELECT tenant_id, phone_number_id, business_account_id, access_token
                        FROM whatsapp_accounts
                        WHERE tenant_id = ?
                        LIMIT 1
                        """,
                (rs, rowNum) -> new WhatsAppAccountConfig(
                        rs.getString("tenant_id"),
                        rs.getString("phone_number_id"),
                        rs.getString("business_account_id"),
                        rs.getString("access_token")
                ),
                tenantId
        );
        return rows.stream().findFirst();
    }
}
