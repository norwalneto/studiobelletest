package com.nwltecnologia.studiobelle.customer.repository;

import com.nwltecnologia.studiobelle.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findAllByTenantIdOrderByNameAsc(String tenantId);
    Optional<Customer> findByIdAndTenantId(Long id, String tenantId);
    Optional<Customer> findByPhoneAndTenantId(String phone, String tenantId);
    List<Customer> findAllByTenantIdAndLastVisitBefore(String tenantId, LocalDateTime threshold);
}
