package com.nwltecnologia.studiobelle.professional.service;

import com.nwltecnologia.studiobelle.common.exception.BusinessException;
import com.nwltecnologia.studiobelle.common.tenant.TenantSupport;
import com.nwltecnologia.studiobelle.professional.dto.ProfessionalRequest;
import com.nwltecnologia.studiobelle.professional.dto.ProfessionalResponse;
import com.nwltecnologia.studiobelle.professional.entity.Professional;
import com.nwltecnologia.studiobelle.professional.repository.ProfessionalRepository;
import com.nwltecnologia.studiobelle.servicecatalog.entity.ServiceOffering;
import com.nwltecnologia.studiobelle.servicecatalog.service.ServiceOfferingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProfessionalService {
    private final ProfessionalRepository repository;
    private final TenantSupport tenantSupport;
    private final ServiceOfferingService serviceOfferingService;

    public ProfessionalService(ProfessionalRepository repository, TenantSupport tenantSupport, ServiceOfferingService serviceOfferingService) {
        this.repository = repository;
        this.tenantSupport = tenantSupport;
        this.serviceOfferingService = serviceOfferingService;
    }

    public List<ProfessionalResponse> findAll() {
        return repository.findAllByTenantIdOrderByNameAsc(tenantSupport.currentTenant()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ProfessionalResponse create(ProfessionalRequest request) {
        String tenant = tenantSupport.currentTenant();
        Professional professional = Professional.builder()
                .tenantId(tenant)
                .name(request.name())
                .workingHours(request.workingHours())
                .services(resolveServices(request.serviceIds(), tenant))
                .build();
        return toResponse(repository.save(professional));
    }

    @Transactional
    public ProfessionalResponse update(Long id, ProfessionalRequest request) {
        String tenant = tenantSupport.currentTenant();
        Professional professional = repository.findByIdAndTenantId(id, tenant)
                .orElseThrow(() -> new BusinessException("Profissional não encontrado"));
        professional.setName(request.name());
        professional.setWorkingHours(request.workingHours());
        professional.setServices(resolveServices(request.serviceIds(), tenant));
        return toResponse(repository.save(professional));
    }

    @Transactional
    public void delete(Long id) {
        Professional professional = repository.findByIdAndTenantId(id, tenantSupport.currentTenant())
                .orElseThrow(() -> new BusinessException("Profissional não encontrado"));
        repository.delete(professional);
    }

    public Professional mustFindByTenant(Long id, String tenant) {
        return repository.findByIdAndTenantId(id, tenant).orElseThrow(() -> new BusinessException("Profissional não encontrado"));
    }

    private Set<ServiceOffering> resolveServices(Set<Long> ids, String tenant) {
        Set<ServiceOffering> services = new HashSet<>();
        for (Long id : ids) {
            services.add(serviceOfferingService.mustFindByTenant(id, tenant));
        }
        return services;
    }

    private ProfessionalResponse toResponse(Professional professional) {
        return new ProfessionalResponse(professional.getId(), professional.getTenantId(), professional.getName(),
                professional.getWorkingHours(), professional.getServices().stream().map(ServiceOffering::getId).collect(java.util.stream.Collectors.toSet()),
                professional.getCreatedAt(), professional.getUpdatedAt());
    }
}
