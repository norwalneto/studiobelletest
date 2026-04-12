package com.nwltecnologia.studiobelle.customer.service;

import com.nwltecnologia.studiobelle.common.exception.BusinessException;
import com.nwltecnologia.studiobelle.common.tenant.TenantSupport;
import com.nwltecnologia.studiobelle.customer.dto.CustomerHistoryResponse;
import com.nwltecnologia.studiobelle.customer.dto.CustomerRequest;
import com.nwltecnologia.studiobelle.customer.dto.CustomerResponse;
import com.nwltecnologia.studiobelle.customer.entity.Customer;
import com.nwltecnologia.studiobelle.customer.entity.CustomerHistory;
import com.nwltecnologia.studiobelle.customer.repository.CustomerHistoryRepository;
import com.nwltecnologia.studiobelle.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository repository;
    private final CustomerHistoryRepository historyRepository;
    private final TenantSupport tenantSupport;

    public CustomerService(CustomerRepository repository, CustomerHistoryRepository historyRepository, TenantSupport tenantSupport) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.tenantSupport = tenantSupport;
    }

    public List<CustomerResponse> findAll() {
        return repository.findAllByTenantIdOrderByNameAsc(tenantSupport.currentTenant()).stream().map(this::toResponse).toList();
    }

    public CustomerResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        Customer customer = Customer.builder()
                .tenantId(tenantSupport.currentTenant())
                .name(request.name())
                .phone(request.phone())
                .email(request.email())
                .notes(request.notes())
                .totalSpent(BigDecimal.ZERO)
                .frequency("NOVO")
                .build();
        return toResponse(repository.save(customer));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findEntity(id);
        customer.setName(request.name());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
        customer.setNotes(request.notes());
        return toResponse(repository.save(customer));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findEntity(id));
    }

    public List<CustomerResponse> findInactive(int inactivityDays) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(inactivityDays);
        return repository.findAllByTenantIdAndLastVisitBefore(tenantSupport.currentTenant(), threshold)
                .stream().map(this::toResponse).toList();
    }

    public List<CustomerHistoryResponse> findHistory(Long customerId) {
        Customer customer = findEntity(customerId);
        return historyRepository.findAllByTenantIdAndCustomerIdOrderByAttendedAtDesc(customer.getTenantId(), customer.getId())
                .stream().map(this::toHistoryResponse).toList();
    }

    @Transactional
    public Customer createOrUpdateByPhone(String tenantId, String phone, String name) {
        return repository.findByPhoneAndTenantId(phone, tenantId)
                .map(existing -> {
                    if (name != null && !name.isBlank()) existing.setName(name);
                    return repository.save(existing);
                })
                .orElseGet(() -> repository.save(Customer.builder()
                        .tenantId(tenantId)
                        .phone(phone)
                        .name((name == null || name.isBlank()) ? "Cliente" : name)
                        .totalSpent(BigDecimal.ZERO)
                        .frequency("NOVO")
                        .build()));
    }

    @Transactional
    public void registerAttendance(Customer customer, String serviceName, BigDecimal amount, LocalDateTime when, String notes) {
        customer.setLastVisit(when);
        customer.setTotalSpent(customer.getTotalSpent().add(amount));
        customer.setFrequency(calculateFrequency(customer));
        repository.save(customer);

        CustomerHistory history = CustomerHistory.builder()
                .tenantId(customer.getTenantId())
                .customer(customer)
                .serviceName(serviceName)
                .amount(amount)
                .attendedAt(when)
                .notes(notes)
                .build();
        historyRepository.save(history);
    }

    private String calculateFrequency(Customer customer) {
        if (customer.getLastVisit() == null) {
            return "NOVO";
        }
        long days = ChronoUnit.DAYS.between(customer.getLastVisit(), LocalDateTime.now());
        if (days <= 30) return "ALTA";
        if (days <= 60) return "MÉDIA";
        return "BAIXA";
    }

    private Customer findEntity(Long id) {
        return repository.findByIdAndTenantId(id, tenantSupport.currentTenant())
                .orElseThrow(() -> new BusinessException("Cliente não encontrado"));
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getTenantId(), customer.getName(), customer.getPhone(), customer.getEmail(),
                customer.getLastVisit(), customer.getFrequency(), customer.getTotalSpent(), customer.getNotes(), customer.getCreatedAt(), customer.getUpdatedAt());
    }

    private CustomerHistoryResponse toHistoryResponse(CustomerHistory history) {
        return new CustomerHistoryResponse(history.getId(), history.getCustomer().getId(), history.getServiceName(), history.getAmount(),
                history.getAttendedAt(), history.getNotes(), history.getCreatedAt());
    }
}
