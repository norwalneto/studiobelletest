package com.nwltecnologia.studiobelle.tenantmaster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    public List<Tenant> getAllTenants() {
        return tenantService.findAll();
    }

    @GetMapping("/{id}")
    public Tenant getTenantById(@PathVariable Long id) {
        return tenantService.findById(id);
    }

    @PostMapping
    public ResponseEntity<String> criarTenant(@RequestBody TenantRequest request) {
        tenantService.criarTenant(request);
        return ResponseEntity.ok("Tenant criado com sucesso!");
    }
}
