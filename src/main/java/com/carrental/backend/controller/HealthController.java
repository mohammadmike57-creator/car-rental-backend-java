package com.carrental.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<String> rootHealth() {
        System.out.println("Health check at /");
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        System.out.println("Health check at /health");
        return ResponseEntity.ok("OK");
    }
}
