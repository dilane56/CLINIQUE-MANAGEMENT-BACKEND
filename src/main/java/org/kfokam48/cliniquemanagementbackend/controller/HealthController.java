package org.kfokam48.cliniquemanagementbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Page d'accueil de l'API. L'état de santé réel (base de données comprise) est fourni par
 * Actuator sur /actuator/health : les anciennes routes /health et /actuator/health de cette
 * classe répondaient toujours "UP" sans rien vérifier et ont été supprimées.
 */
@RestController
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Clinique Management API is running");
        response.put("health", "/actuator/health");
        return ResponseEntity.ok(response);
    }
}
