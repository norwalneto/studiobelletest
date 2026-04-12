package com.nwltecnologia.studiobelle.servicecatalog.repository;

import com.nwltecnologia.studiobelle.servicecatalog.entity.ServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {
    List<ServiceOffering> findAllByTenantIdOrderByNameAsc(String tenantId);
    Optional<ServiceOffering> findByIdAndTenantId(Long id, String tenantId);
}
