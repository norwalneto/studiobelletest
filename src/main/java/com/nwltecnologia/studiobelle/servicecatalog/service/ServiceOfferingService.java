package com.nwltecnologia.studiobelle.servicecatalog.service;

import com.nwltecnologia.studiobelle.common.exception.BusinessException;
import com.nwltecnologia.studiobelle.common.tenant.TenantSupport;
import com.nwltecnologia.studiobelle.servicecatalog.dto.ServiceOfferingRequest;
import com.nwltecnologia.studiobelle.servicecatalog.dto.ServiceOfferingResponse;
import com.nwltecnologia.studiobelle.servicecatalog.entity.ServiceOffering;
import com.nwltecnologia.studiobelle.servicecatalog.repository.ServiceOfferingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiceOfferingService {
    private final ServiceOfferingRepository repository;
    private final TenantSupport tenantSupport;

    public ServiceOfferingService(ServiceOfferingRepository repository, TenantSupport tenantSupport) {
        this.repository = repository;
        this.tenantSupport = tenantSupport;
    }

    public List<ServiceOfferingResponse> findAll() {
        return repository.findAllByTenantIdOrderByNameAsc(tenantSupport.currentTenant()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ServiceOfferingResponse create(ServiceOfferingRequest request) {
        ServiceOffering entity = ServiceOffering.builder()
                .tenantId(tenantSupport.currentTenant())
                .name(request.name())
                .durationMinutes(request.durationMinutes())
                .price(request.price())
                .active(request.active())
                .build();
        return toResponse(repository.save(entity));
    }

    @Transactional
    public ServiceOfferingResponse update(Long id, ServiceOfferingRequest request) {
        ServiceOffering entity = repository.findByIdAndTenantId(id, tenantSupport.currentTenant())
                .orElseThrow(() -> new BusinessException("Serviço não encontrado"));
        entity.setName(request.name());
        entity.setDurationMinutes(request.durationMinutes());
        entity.setPrice(request.price());
        entity.setActive(request.active());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        ServiceOffering entity = repository.findByIdAndTenantId(id, tenantSupport.currentTenant())
                .orElseThrow(() -> new BusinessException("Serviço não encontrado"));
        repository.delete(entity);
    }

    public ServiceOffering mustFindByTenant(Long id, String tenant) {
        return repository.findByIdAndTenantId(id, tenant).orElseThrow(() -> new BusinessException("Serviço não encontrado"));
    }

    private ServiceOfferingResponse toResponse(ServiceOffering entity) {
        return new ServiceOfferingResponse(entity.getId(), entity.getTenantId(), entity.getName(), entity.getDurationMinutes(),
                entity.getPrice(), entity.getActive(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
