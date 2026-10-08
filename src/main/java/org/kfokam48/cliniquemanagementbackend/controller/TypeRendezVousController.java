package org.kfokam48.cliniquemanagementbackend.controller;
import org.springframework.security.access.prepost.PreAuthorize;

import org.kfokam48.cliniquemanagementbackend.dto.typeRendezVous.TypeRendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.dto.typeRendezVous.TypeRendezVousResponseDTO;
import org.kfokam48.cliniquemanagementbackend.service.TypeRendezVousService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/type-rendezvous")
public class TypeRendezVousController {
    @Autowired
    private TypeRendezVousService typeRendezVousService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public TypeRendezVousResponseDTO ajouter(@RequestBody TypeRendezVousDTO dto) {
        return typeRendezVousService.ajouterTypeRendezVous(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public TypeRendezVousResponseDTO modifier(@PathVariable Long id, @RequestBody TypeRendezVousDTO dto) {
        return typeRendezVousService.modifierTypeRendezVous(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public void supprimer(@PathVariable Long id) {
        typeRendezVousService.supprimerTypeRendezVous(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE','MEDECIN')")
    public List<TypeRendezVousResponseDTO> lister() {
        return typeRendezVousService.listerTypeRendezVous();
    }
}

