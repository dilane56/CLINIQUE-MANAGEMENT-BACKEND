package org.kfokam48.cliniquemanagementbackend.controlleur;

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
    public LigneFactureResponseDTO ajouter(@RequestBody LigneFactureDTO dto) {
        return service.ajouterLigne(dto);
    }

    @PutMapping("/{id}")
    public LigneFactureResponseDTO modifier(@PathVariable Long id, @RequestBody LigneFactureDTO dto) {
        return service.modifierLigne(id, dto);
    }

    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id) {
        service.supprimerLigne(id);
    }

    @GetMapping("/{id}")
    public LigneFactureResponseDTO getLigne(@PathVariable Long id) {
        return service.getLigne(id);
    }

    @GetMapping
    public List<LigneFactureResponseDTO> lister() {
        return service.listerLignes();
    }
}

