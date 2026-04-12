package com.nwltecnologia.studiobelle.customer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customers_tenant", columnList = "tenant_id"),
        @Index(name = "idx_customers_phone_tenant", columnList = "phone,tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 40)
    private String phone;

    @Column(length = 180)
    private String email;

    private LocalDateTime lastVisit;

    @Column(length = 80)
    private String frequency;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSpent;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (totalSpent == null) totalSpent = BigDecimal.ZERO;
    }

    @PreUpdate
    public void preUpdate() { updatedAt = Instant.now(); }
}
