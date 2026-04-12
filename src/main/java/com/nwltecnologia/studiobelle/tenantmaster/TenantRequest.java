package com.nwltecnologia.studiobelle.tenantmaster;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantRequest {

    private String nome;
    private String tenantId;
    private String subdomain;
    private String phoneNumberId;
    private String businessAccountId;
    private String accessToken;
}
