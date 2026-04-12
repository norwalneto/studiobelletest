package com.nwltecnologia.studiobelle.appointment.repository;

import com.nwltecnologia.studiobelle.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findAllByTenantIdOrderByStartTimeAsc(String tenantId);

    Optional<Appointment> findByIdAndTenantId(Long id, String tenantId);

    boolean existsByTenantIdAndStartTimeLessThanAndEndTimeGreaterThan(String tenantId, LocalDateTime requestedEnd, LocalDateTime requestedStart);

    boolean existsByTenantIdAndProfessionalIdAndStartTimeLessThanAndEndTimeGreaterThan(String tenantId, Long professionalId,
                                                                                       LocalDateTime requestedEnd,
                                                                                       LocalDateTime requestedStart);
}
