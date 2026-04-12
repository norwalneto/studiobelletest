package com.nwltecnologia.studiobelle.automation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "automations", indexes = @Index(name = "idx_automations_tenant", columnList = "tenant_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Automation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AutomationType type;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false, length = 500)
    private String template;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() { Instant now = Instant.now(); createdAt = now; updatedAt = now; if (active == null) active = true; }

    @PreUpdate
    public void preUpdate() { updatedAt = Instant.now(); }
}
