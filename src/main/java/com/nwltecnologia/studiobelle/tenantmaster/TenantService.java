package com.nwltecnologia.studiobelle.tenantmaster;

import com.nwltecnologia.studiobelle.tenant.DataSourceFactory;
import com.nwltecnologia.studiobelle.tenant.TenantRoutingDataSource;
import com.nwltecnologia.studiobelle.util.StringUtils;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

@Service
public class TenantService {

    private final DataSource masterDataSource;

    private final TenantRepository tenantRepository;

    private final TenantRoutingDataSource routingDataSource;

    private final DataSourceFactory dataSourceFactory;

    public TenantService(DataSource masterDataSource, TenantRepository tenantRepository, TenantRoutingDataSource routingDataSource, DataSourceFactory dataSourceFactory) {
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
        tenant.setDatabaseName(dbName);
        tenant.setUsername("postgres");
        tenant.setPassword("postgres");

        tenantRepository.save(tenant);

        // 4. adiciona no routing
        DataSource ds = dataSourceFactory.createDataSource(tenant);
        routingDataSource.addTenant(tenantId, ds); // 🔥 CORRETO
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
        return input.toLowerCase().replaceAll("[^a-z0-9_]", "_");
    }
}
