package com.nwltecnologia.studiobelle.automation.repository;

import com.nwltecnologia.studiobelle.automation.entity.Automation;
import com.nwltecnologia.studiobelle.automation.entity.AutomationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AutomationRepository extends JpaRepository<Automation, Long> {
    List<Automation> findAllByTenantIdOrderByTypeAsc(String tenantId);
    Optional<Automation> findByTenantIdAndType(String tenantId, AutomationType type);
}
