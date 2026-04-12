package com.nwltecnologia.studiobelle.tenantmaster;

import com.nwltecnologia.studiobelle.tenant.DataSourceFactory;
import com.nwltecnologia.studiobelle.tenant.TenantRoutingDataSource;
import com.nwltecnologia.studiobelle.util.StringUtils;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Service
public class TenantService {

    private final DataSource masterDataSource;

    private final TenantRepository tenantRepository;

    private final TenantRoutingDataSource routingDataSource;

    private final DataSourceFactory dataSourceFactory;

    public TenantService(@Qualifier("masterDataSource") DataSource masterDataSource, TenantRepository tenantRepository, TenantRoutingDataSource routingDataSource, DataSourceFactory dataSourceFactory) {
        this.masterDataSource = masterDataSource;
        this.tenantRepository = tenantRepository;
        this.routingDataSource = routingDataSource;
        this.dataSourceFactory = dataSourceFactory;
    }

    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }

    public Tenant findById(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));
    }

    public void criarTenant(TenantRequest request) {

        // 🔥 separa responsabilidade
        String tenantId = sanitize(request.getTenantId());
        String dbName = StringUtils.sanitize("db_" + request.getNome());

        // 1. cria banco
        criarBanco(dbName);

        // 2. roda flyway
        rodarFlyway(dbName);

        // 3. salva no master
        Tenant tenant = new Tenant();
        tenant.setNome(request.getNome());
        tenant.setTenantId(tenantId); // 🔥 NOVO CAMPO
        tenant.setSubdomain(sanitize(request.getSubdomain()));
        tenant.setDatabaseName(dbName);
        tenant.setUsername("postgres");
        tenant.setPassword("postgres");
        tenant.setPhoneNumberId(request.getPhoneNumberId());
        tenant.setBusinessAccountId(request.getBusinessAccountId());

        tenantRepository.save(tenant);

        if (request.getPhoneNumberId() != null && !request.getPhoneNumberId().isBlank()) {
            saveWhatsAppAccount(tenantId, request.getPhoneNumberId(), request.getBusinessAccountId(), request.getAccessToken());
        }

        // 4. adiciona no routing
        DataSource ds = dataSourceFactory.createDataSource(tenant);
        routingDataSource.addTenant(tenantId, ds); // 🔥 CORRETO
    }


    private void saveWhatsAppAccount(String tenantId, String phoneNumberId, String businessAccountId, String accessToken) {
        String sql = """
                INSERT INTO whatsapp_accounts (tenant_id, phone_number_id, business_account_id, access_token)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (tenant_id)
                DO UPDATE SET phone_number_id = EXCLUDED.phone_number_id,
                              business_account_id = EXCLUDED.business_account_id,
                              access_token = EXCLUDED.access_token
                """;

        try (Connection conn = masterDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenantId);
            ps.setString(2, phoneNumberId);
            ps.setString(3, businessAccountId);
            ps.setString(4, accessToken);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void criarBanco(String dbName) {
        try (Connection conn = masterDataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE DATABASE " + dbName);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void rodarFlyway(String dbName) {
        Flyway flyway = Flyway.configure()
                .dataSource("jdbc:postgresql://localhost:5432/" + dbName,
                        "postgres",
                        "postgres")
                .load();

        flyway.migrate();
    }

    private String sanitize(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        return input.toLowerCase().replaceAll("[^a-z0-9_]", "_");
    }
}
