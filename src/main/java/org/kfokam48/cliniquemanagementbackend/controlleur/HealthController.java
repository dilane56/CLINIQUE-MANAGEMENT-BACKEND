package org.kfokam48.cliniquemanagementbackend.controlleur;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "CLINIQUE-MANAGEMENT-BACKEND");
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/actuator/health")
    public ResponseEntity<Map<String, String>> actuatorHealth() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Clinique Management API is running");
        response.put("status", "UP");
        return ResponseEntity.ok(response);
    }
}