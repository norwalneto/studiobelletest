package com.nwltecnologia.studiobelle.professional.repository;

import com.nwltecnologia.studiobelle.professional.entity.Professional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfessionalRepository extends JpaRepository<Professional, Long> {
    List<Professional> findAllByTenantIdOrderByNameAsc(String tenantId);
    Optional<Professional> findByIdAndTenantId(Long id, String tenantId);
}
