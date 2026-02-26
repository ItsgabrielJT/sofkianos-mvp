package com.sofkianos.producer.infrastructure.inbound.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health and root-path controller.
 * Handles {@code GET /} to prevent {@code NoHandlerFoundException}
 * noise from health checks and load balancers.
 */
@RestController
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(Map.of(
                "service", "producer-api",
                "status", "UP"
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "service", "producer-api",
                "status", "UP"
        ));
    }
}
