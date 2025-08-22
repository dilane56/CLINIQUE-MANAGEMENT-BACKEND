package org.kfokam48.cliniquemanagementbackend.controlleur;


import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousUpdateDto;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.service.impl.RendezVousServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rendezvous")
public class RendezVousController {
    private final RendezVousServiceImpl rendezVousService;

    public RendezVousController(RendezVousServiceImpl rendezVousService) {
        this.rendezVousService = rendezVousService;
    }
    @PostMapping
    public ResponseEntity<RendezVousResponseDTO> createRendezVous(@Valid @RequestBody RendezVousDTO rendezVousDTO) {
        RendezVousResponseDTO rendezVous = rendezVousService.save(rendezVousDTO);
        return ResponseEntity.ok(rendezVous);
    }

    @GetMapping
    public ResponseEntity<List<RendezVousResponseDTO>> getAllRendezVous() {
        List<RendezVousResponseDTO> rendezVousList = rendezVousService.findAll();
        return ResponseEntity.ok(rendezVousList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RendezVousResponseDTO> getRendezVousById(@PathVariable Long id) {
        RendezVousResponseDTO rendezVous = rendezVousService.findById(id);
        return ResponseEntity.ok(rendezVous);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RendezVousResponseDTO> updateRendezVous(@PathVariable Long id, @Valid @RequestBody RendezVousUpdateDto rendezVousDTO) {
        RendezVousResponseDTO updatedRendezVous = rendezVousService.update(id, rendezVousDTO);
        return ResponseEntity.ok(updatedRendezVous);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRendezVous(@PathVariable Long id) {
        return rendezVousService.deleteById(id);
    }

    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<List<RendezVousResponseDTO>> getRendezVousByMedecin(@PathVariable Long medecinId) {
        List<RendezVousResponseDTO> rendezVousList = rendezVousService.findByMedecinId(medecinId);
        return ResponseEntity.ok(rendezVousList);
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<RendezVousResponseDTO> updateStatutRendezVous(@PathVariable Long id, @RequestParam("statut") StatutRendezVous statut) {
        RendezVousResponseDTO updatedRendezVous = rendezVousService.updateStatut(id, statut);
        return ResponseEntity.ok(updatedRendezVous);
    }

    @GetMapping("/medecin/{medecinId}/aujourd'hui")
    public ResponseEntity<List<RendezVousResponseDTO>> getRendezVousDuJourByMedecin(@PathVariable Long medecinId) {
        List<RendezVousResponseDTO> rendezVousList = rendezVousService.findRendezVousDuJourByMedecin(medecinId);
        return ResponseEntity.ok(rendezVousList);
    }
}
