package com.nwltecnologia.studiobelle.automation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "tenant_settings", indexes = @Index(name = "idx_tenant_settings_tenant", columnList = "tenant_id", unique = true))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, unique = true, length = 100)
    private String tenantId;

    @Column(nullable = false)
    private Integer inactivityDays;

    @Column(nullable = false)
    private Boolean aiEnabled;

    @Column(nullable = false)
    private Boolean automationEnabled;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (inactivityDays == null) inactivityDays = 30;
        if (aiEnabled == null) aiEnabled = false;
        if (automationEnabled == null) automationEnabled = true;
    }

    @PreUpdate
    public void preUpdate() { updatedAt = Instant.now(); }
}
