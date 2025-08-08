package org.kfokam48.cliniquemanagementbackend.controlleur;


import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.secretaire.SecretaireDTO;
import org.kfokam48.cliniquemanagementbackend.dto.secretaire.SecretaireResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Secretaire;
import org.kfokam48.cliniquemanagementbackend.service.impl.SecretaireServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/secretaires")
public class SecretaireController {
    private final SecretaireServiceImpl secretaireService;

    public SecretaireController(SecretaireServiceImpl secretaireService) {
        this.secretaireService = secretaireService;
    }
    @PostMapping
    @PreAuthorize("hasRole('MEDECIN') or hasRole('ADMIN')") // Accès pour les rôles MEDECIN et ADMIN
    public ResponseEntity<SecretaireResponseDTO> createSecretaire(@Valid @RequestBody SecretaireDTO secretaireDTO) {
        SecretaireResponseDTO secretaire = secretaireService.save(secretaireDTO);
        return ResponseEntity.ok(secretaire);
    }

    @GetMapping
    @PreAuthorize("hasRole('MEDECIN') or hasRole('ADMIN')") // Accès pour les rôles SECRETAIRE et ADMIN
    public ResponseEntity<List<SecretaireResponseDTO>> getAllSecretaires() {
        List<SecretaireResponseDTO> secretaires = secretaireService.findAll();
        return ResponseEntity.ok(secretaires);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SECRETAIRE') or hasRole('ADMIN')")
    public ResponseEntity<SecretaireResponseDTO> getSecretaireById(@PathVariable Long id) {
        SecretaireResponseDTO secretaire = secretaireService.findById(id);
        return ResponseEntity.ok(secretaire);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SECRETAIRE') or hasRole('ADMIN')")
    public ResponseEntity<SecretaireResponseDTO> updateSecretaire(@PathVariable Long id,@Valid @RequestBody SecretaireDTO secretaireDTO) {
        SecretaireResponseDTO updatedSecretaire = secretaireService.update(id, secretaireDTO);
        return ResponseEntity.ok(updatedSecretaire);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDECIN')")
    public ResponseEntity<String> deleteSecretaire(@PathVariable Long id) {
        return secretaireService.deleteById(id);
    }


}
