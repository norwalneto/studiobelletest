package com.nwltecnologia.studiobelle.automation.repository;

import com.nwltecnologia.studiobelle.automation.entity.TenantSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantSettingsRepository extends JpaRepository<TenantSettings, Long> {
    Optional<TenantSettings> findByTenantId(String tenantId);
}
