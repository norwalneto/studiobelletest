package com.nwltecnologia.studiobelle.tenantmaster;

import jakarta.persistence.*;
import lombok.*;

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

    private String nome;
    private String tenantId;
    private String databaseName;
    private String username;
    private String password;


}
