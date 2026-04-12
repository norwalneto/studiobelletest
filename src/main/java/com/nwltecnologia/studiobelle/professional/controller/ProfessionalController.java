package com.nwltecnologia.studiobelle.professional.controller;

import com.nwltecnologia.studiobelle.common.dto.ApiMessageResponse;
import com.nwltecnologia.studiobelle.professional.dto.ProfessionalRequest;
import com.nwltecnologia.studiobelle.professional.dto.ProfessionalResponse;
import com.nwltecnologia.studiobelle.professional.service.ProfessionalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/professionals")
public class ProfessionalController {

    private final ProfessionalService service;

    public ProfessionalController(ProfessionalService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ProfessionalResponse>> findAll() { return ResponseEntity.ok(service.findAll()); }

    @PostMapping
    public ResponseEntity<ProfessionalResponse> create(@Valid @RequestBody ProfessionalRequest request) { return ResponseEntity.ok(service.create(request)); }

    @PutMapping("/{id}")
    public ResponseEntity<ProfessionalResponse> update(@PathVariable Long id, @Valid @RequestBody ProfessionalRequest request) { return ResponseEntity.ok(service.update(id, request)); }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiMessageResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiMessageResponse("Profissional removido com sucesso"));
    }
}
