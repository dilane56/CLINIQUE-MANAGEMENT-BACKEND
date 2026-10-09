package org.kfokam48.cliniquemanagementbackend.controller;


import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.secretaire.SecretaireDTO;
import org.kfokam48.cliniquemanagementbackend.dto.secretaire.SecretaireResponseDTO;
import org.kfokam48.cliniquemanagementbackend.service.SecretaireService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/secretaires")
public class SecretaireController {
    private final SecretaireService secretaireService;

    public SecretaireController(SecretaireService secretaireService) {
        this.secretaireService = secretaireService;
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN')")
    public ResponseEntity<SecretaireResponseDTO> createSecretaire(@Valid @RequestBody SecretaireDTO secretaireDTO) {
        SecretaireResponseDTO secretaire = secretaireService.save(secretaireDTO);
        return ResponseEntity.ok(secretaire);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN')")
    public ResponseEntity<List<SecretaireResponseDTO>> getAllSecretaires() {
        List<SecretaireResponseDTO> secretaires = secretaireService.findAll();
        return ResponseEntity.ok(secretaires);
    }

    // Variante paginée (?page=0&size=20&sort=...) ; sans "page", la liste complète ci-dessus reste servie
    @GetMapping(params = "page")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN')")
    public ResponseEntity<PageResponse<SecretaireResponseDTO>> getAllSecretairesPage(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(secretaireService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN') or (hasRole('SECRETAIRE') and @authz.isCurrentUser(#id))")
    public ResponseEntity<SecretaireResponseDTO> getSecretaireById(@PathVariable Long id) {
        SecretaireResponseDTO secretaire = secretaireService.findById(id);
        return ResponseEntity.ok(secretaire);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SECRETAIRE') and @authz.isCurrentUser(#id))")
    public ResponseEntity<SecretaireResponseDTO> updateSecretaire(@PathVariable Long id,@Valid @RequestBody SecretaireDTO secretaireDTO) {
        SecretaireResponseDTO updatedSecretaire = secretaireService.update(id, secretaireDTO);
        return ResponseEntity.ok(updatedSecretaire);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN')")
    public ResponseEntity<String> deleteSecretaire(@PathVariable Long id) {
        secretaireService.deleteById(id);
        return ResponseEntity.ok("Secrétaire supprimée");
    }


}
