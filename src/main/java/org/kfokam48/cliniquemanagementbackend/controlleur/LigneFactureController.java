package org.kfokam48.cliniquemanagementbackend.controlleur;
import org.springframework.security.access.prepost.PreAuthorize;

import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureDTO;
import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureResponseDTO;
import org.kfokam48.cliniquemanagementbackend.service.LigneFactureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/lignes-facture")
public class LigneFactureController {
    @Autowired
    private LigneFactureService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public LigneFactureResponseDTO ajouter(@RequestBody LigneFactureDTO dto) {
        return service.ajouterLigne(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public LigneFactureResponseDTO modifier(@PathVariable Long id, @RequestBody LigneFactureDTO dto) {
        return service.modifierLigne(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public void supprimer(@PathVariable Long id) {
        service.supprimerLigne(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE','MEDECIN')")
    public LigneFactureResponseDTO getLigne(@PathVariable Long id) {
        return service.getLigne(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE','MEDECIN')")
    public List<LigneFactureResponseDTO> lister() {
        return service.listerLignes();
    }
}

