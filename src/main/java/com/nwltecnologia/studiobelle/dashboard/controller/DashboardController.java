package com.nwltecnologia.studiobelle.dashboard.controller;

import com.nwltecnologia.studiobelle.dashboard.dto.DashboardResponse;
import com.nwltecnologia.studiobelle.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/metrics")
    public ResponseEntity<DashboardResponse> metrics() {
        return ResponseEntity.ok(service.getMetrics());
    }
}
