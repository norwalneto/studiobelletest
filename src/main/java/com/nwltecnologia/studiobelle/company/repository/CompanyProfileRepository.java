package com.nwltecnologia.studiobelle.company.repository;

import com.nwltecnologia.studiobelle.company.entity.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {
    Optional<CompanyProfile> findByTenantId(String tenantId);
}
