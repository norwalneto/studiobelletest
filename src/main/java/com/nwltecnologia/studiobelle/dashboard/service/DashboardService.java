package com.nwltecnologia.studiobelle.dashboard.service;

import com.nwltecnologia.studiobelle.appointment.repository.AppointmentRepository;
import com.nwltecnologia.studiobelle.common.tenant.TenantSupport;
import com.nwltecnologia.studiobelle.customer.repository.CustomerRepository;
import com.nwltecnologia.studiobelle.dashboard.dto.DashboardResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DashboardService {

    private final TenantSupport tenantSupport;
    private final CustomerRepository customerRepository;
    private final AppointmentRepository appointmentRepository;

    public DashboardService(TenantSupport tenantSupport,
                            CustomerRepository customerRepository,
                            AppointmentRepository appointmentRepository) {
        this.tenantSupport = tenantSupport;
        this.customerRepository = customerRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public DashboardResponse getMetrics() {
        String tenant = tenantSupport.currentTenant();
        long totalCustomers = customerRepository.findAllByTenantIdOrderByNameAsc(tenant).size();
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        long inactiveCustomers = customerRepository.findAllByTenantIdAndLastVisitBefore(tenant, threshold).size();
        long activeCustomers = Math.max(0, totalCustomers - inactiveCustomers);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        long appointmentsToday = appointmentRepository.findAllByTenantIdOrderByStartTimeAsc(tenant).stream()
                .filter(a -> !a.getStartTime().isBefore(startOfDay) && a.getStartTime().isBefore(endOfDay))
                .count();

        long freeSlotsToday = Math.max(0, 16 - appointmentsToday);

        BigDecimal estimatedRevenue = appointmentRepository.findAllByTenantIdOrderByStartTimeAsc(tenant).stream()
                .filter(a -> !a.getStartTime().isBefore(startOfDay) && a.getStartTime().isBefore(endOfDay))
                .map(a -> a.getServicePrice() == null ? BigDecimal.ZERO : a.getServicePrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardResponse(totalCustomers, activeCustomers, inactiveCustomers, appointmentsToday, freeSlotsToday, estimatedRevenue);
    }
}
