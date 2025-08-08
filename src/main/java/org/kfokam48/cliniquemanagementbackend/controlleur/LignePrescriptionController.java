package org.kfokam48.cliniquemanagementbackend.controlleur;

import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionDTO;
import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.service.LignePrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/lignes-prescription")
public class LignePrescriptionController {
    @Autowired
    private LignePrescriptionService service;

    @PostMapping
    public LignePrescriptionResponseDTO ajouter(@RequestBody LignePrescriptionDTO dto) {
        return service.ajouterLigne(dto);
    }

    @PutMapping("/{id}")
    public LignePrescriptionResponseDTO modifier(@PathVariable Long id, @RequestBody LignePrescriptionUpdateDTO dto) {
        return service.modifierLigne(id, dto);
    }

    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id) {
        service.supprimerLigne(id);
    }

    @GetMapping("/{id}")
    public LignePrescriptionResponseDTO getLigne(@PathVariable Long id) {
        return service.getLigne(id);
    }

    @GetMapping
    public List<LignePrescriptionResponseDTO> lister() {
        return service.listerLignes();
    }
}

