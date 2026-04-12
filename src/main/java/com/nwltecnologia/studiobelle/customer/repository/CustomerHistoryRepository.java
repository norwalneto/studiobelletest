package com.nwltecnologia.studiobelle.customer.repository;

import com.nwltecnologia.studiobelle.customer.entity.CustomerHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerHistoryRepository extends JpaRepository<CustomerHistory, Long> {
    List<CustomerHistory> findAllByTenantIdAndCustomerIdOrderByAttendedAtDesc(String tenantId, Long customerId);
}
