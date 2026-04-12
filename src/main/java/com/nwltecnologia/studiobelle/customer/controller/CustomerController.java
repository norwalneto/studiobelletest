package com.nwltecnologia.studiobelle.customer.controller;

import com.nwltecnologia.studiobelle.common.dto.ApiMessageResponse;
import com.nwltecnologia.studiobelle.customer.dto.CustomerHistoryResponse;
import com.nwltecnologia.studiobelle.customer.dto.CustomerRequest;
import com.nwltecnologia.studiobelle.customer.dto.CustomerResponse;
import com.nwltecnologia.studiobelle.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> findAll() { return ResponseEntity.ok(service.findAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> findById(@PathVariable Long id) { return ResponseEntity.ok(service.findById(id)); }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<CustomerHistoryResponse>> findHistory(@PathVariable Long id) { return ResponseEntity.ok(service.findHistory(id)); }

    @GetMapping("/inactive")
    public ResponseEntity<List<CustomerResponse>> inactive(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(service.findInactive(days));
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) { return ResponseEntity.ok(service.create(request)); }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) { return ResponseEntity.ok(service.update(id, request)); }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiMessageResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiMessageResponse("Cliente removido com sucesso"));
    }
}
