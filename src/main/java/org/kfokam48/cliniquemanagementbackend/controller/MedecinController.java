package org.kfokam48.cliniquemanagementbackend.controller;


import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.medecin.MedecinDTO;
import org.kfokam48.cliniquemanagementbackend.dto.medecin.MedecinResponseDTO;
import org.kfokam48.cliniquemanagementbackend.mapper.MedecinMapper;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.service.MedecinService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medecins")
public class MedecinController {
    private final MedecinService medecinService;
    private final MedecinMapper medecinMapper;

    public MedecinController(MedecinService medecinService, MedecinMapper medecinMapper) {
        this.medecinService = medecinService;
        this.medecinMapper = medecinMapper;
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedecinResponseDTO> createMedecin(@Valid @RequestBody MedecinDTO medecinDTO) {
        Medecin medecin = medecinService.save(medecinDTO);
        return ResponseEntity.ok(medecinMapper.medecinToMedecinResponseDto(medecin));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public ResponseEntity<List<MedecinResponseDTO>> getAllMedecins() {
        List<MedecinResponseDTO> medecins = medecinService.findAll();
        return ResponseEntity.ok(medecins);
    }

    // Variante paginée (?page=0&size=20&sort=...) ; sans "page", la liste complète ci-dessus reste servie
    @GetMapping(params = "page")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public ResponseEntity<PageResponse<MedecinResponseDTO>> getAllMedecinsPage(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(medecinService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE','MEDECIN')")
    public ResponseEntity<MedecinResponseDTO> getMedecinById(@PathVariable Long id) {
        MedecinResponseDTO medecin = medecinService.findById(id);
        return ResponseEntity.ok(medecin);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MEDECIN') and @authz.isCurrentUser(#id))")
    public ResponseEntity<MedecinResponseDTO> updateMedecin(@PathVariable Long id,@Valid @RequestBody MedecinDTO medecinDTO) {
        MedecinResponseDTO updatedMedecin = medecinService.update(id, medecinDTO);
        return ResponseEntity.ok(updatedMedecin);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteMedecin(@PathVariable Long id) {
        medecinService.deleteById(id);
        return ResponseEntity.ok("Medecin deleted successfully");
    }

}
