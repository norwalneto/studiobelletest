package com.nwltecnologia.studiobelle.tenantmaster;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String nome;

    @Column(name = "tenant_id")
    private String tenantId;

    private String subdomain;

    @Column(name = "database_name")
    private String databaseName;

    private String username;
    private String password;

    @Column(name = "phone_number_id")
    private String phoneNumberId;

    @Column(name = "business_account_id")
    private String businessAccountId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
