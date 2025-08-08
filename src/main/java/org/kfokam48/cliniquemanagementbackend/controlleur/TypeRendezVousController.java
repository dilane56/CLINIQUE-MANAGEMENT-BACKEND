package org.kfokam48.cliniquemanagementbackend.controlleur;

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
    public TypeRendezVousResponseDTO ajouter(@RequestBody TypeRendezVousDTO dto) {
        return typeRendezVousService.ajouterTypeRendezVous(dto);
    }

    @PutMapping("/{id}")
    public TypeRendezVousResponseDTO modifier(@PathVariable Long id, @RequestBody TypeRendezVousDTO dto) {
        return typeRendezVousService.modifierTypeRendezVous(id, dto);
    }

    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id) {
        typeRendezVousService.supprimerTypeRendezVous(id);
    }

    @GetMapping
    public List<TypeRendezVousResponseDTO> lister() {
        return typeRendezVousService.listerTypeRendezVous();
    }
}

