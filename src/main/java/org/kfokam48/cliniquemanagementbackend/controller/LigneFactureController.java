package org.kfokam48.cliniquemanagementbackend.controller;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureDTO;
import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureResponseDTO;
import org.kfokam48.cliniquemanagementbackend.service.LigneFactureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/lignes-facture")
// Les lignes sont toujours créées avec leur facture (POST /api/factures) :
// pas de création isolée, qui produisait des lignes orphelines.
public class LigneFactureController {
    @Autowired
    private LigneFactureService service;


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public LigneFactureResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody LigneFactureDTO dto) {
        return service.modifierLigne(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public void supprimer(@PathVariable Long id) {
        service.supprimerLigne(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE') or (hasRole('MEDECIN') and @authz.ownsLigneFacture(#id))")
    public LigneFactureResponseDTO getLigne(@PathVariable Long id) {
        return service.getLigne(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public List<LigneFactureResponseDTO> lister() {
        return service.listerLignes();
    }
}

