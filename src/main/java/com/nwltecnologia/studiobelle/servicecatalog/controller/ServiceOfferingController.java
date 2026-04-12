package com.nwltecnologia.studiobelle.servicecatalog.controller;

import com.nwltecnologia.studiobelle.common.dto.ApiMessageResponse;
import com.nwltecnologia.studiobelle.servicecatalog.dto.ServiceOfferingRequest;
import com.nwltecnologia.studiobelle.servicecatalog.dto.ServiceOfferingResponse;
import com.nwltecnologia.studiobelle.servicecatalog.service.ServiceOfferingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
public class ServiceOfferingController {

    private final ServiceOfferingService service;

    public ServiceOfferingController(ServiceOfferingService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ServiceOfferingResponse>> findAll() { return ResponseEntity.ok(service.findAll()); }

    @PostMapping
    public ResponseEntity<ServiceOfferingResponse> create(@Valid @RequestBody ServiceOfferingRequest request) { return ResponseEntity.ok(service.create(request)); }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceOfferingResponse> update(@PathVariable Long id, @Valid @RequestBody ServiceOfferingRequest request) { return ResponseEntity.ok(service.update(id, request)); }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiMessageResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiMessageResponse("Serviço removido com sucesso"));
    }
}
