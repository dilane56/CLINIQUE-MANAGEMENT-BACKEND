package org.kfokam48.cliniquemanagementbackend.controller;
import java.util.Map;
import java.time.LocalDate;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import org.springframework.security.access.prepost.PreAuthorize;


import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousUpdateDto;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.service.RendezVousService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rendezvous")
public class RendezVousController {
    private final RendezVousService rendezVousService;

    public RendezVousController(RendezVousService rendezVousService) {
        this.rendezVousService = rendezVousService;
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE') or (hasRole('MEDECIN') and @authz.isCurrentUser(#rendezVousDTO.medecinId))")
    public ResponseEntity<RendezVousResponseDTO> createRendezVous(@Valid @RequestBody RendezVousDTO rendezVousDTO) {
        RendezVousResponseDTO rendezVous = rendezVousService.save(rendezVousDTO);
        return ResponseEntity.ok(rendezVous);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public ResponseEntity<List<RendezVousResponseDTO>> getAllRendezVous() {
        List<RendezVousResponseDTO> rendezVousList = rendezVousService.findAll();
        return ResponseEntity.ok(rendezVousList);
    }

    // Variante paginée (?page=0&size=20&sort=...) ; sans "page", la liste complète ci-dessus reste servie
    @GetMapping(params = "page")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public ResponseEntity<PageResponse<RendezVousResponseDTO>> getAllRendezVousPage(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(rendezVousService.findAll(pageable));
    }

    /**
     * Recherche paginée, exécutée par la base : texte (patient, médecin, type de rendez-vous),
     * statut et jour facultatifs. Exemple :
     * /api/rendezvous/recherche?q=martin&statut=CONFIRME&date=2030-01-15&page=0&size=20&sort=dateRendezVous,desc
     */
    @GetMapping("/recherche")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public ResponseEntity<PageResponse<RendezVousResponseDTO>> rechercherRendezVous(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) StatutRendezVous statut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 20, sort = "dateRendezVous", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(rendezVousService.rechercher(q, statut, date, pageable));
    }

    // Nombre de rendez-vous par statut (compteurs) : { "EN_ATTENTE": 12, "CONFIRME": 23, ... }
    @GetMapping("/statistiques")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public ResponseEntity<Map<StatutRendezVous, Long>> statistiquesRendezVous() {
        return ResponseEntity.ok(rendezVousService.compterParStatut());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE') or (hasRole('MEDECIN') and @authz.ownsRendezVous(#id))")
    public ResponseEntity<RendezVousResponseDTO> getRendezVousById(@PathVariable Long id) {
        RendezVousResponseDTO rendezVous = rendezVousService.findById(id);
        return ResponseEntity.ok(rendezVous);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE') or (hasRole('MEDECIN') and @authz.ownsRendezVous(#id) and @authz.isCurrentUser(#rendezVousDTO.medecinId))")
    public ResponseEntity<RendezVousResponseDTO> updateRendezVous(@PathVariable Long id, @Valid @RequestBody RendezVousUpdateDto rendezVousDTO) {
        RendezVousResponseDTO updatedRendezVous = rendezVousService.update(id, rendezVousDTO);
        return ResponseEntity.ok(updatedRendezVous);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public ResponseEntity<String> deleteRendezVous(@PathVariable Long id) {
        rendezVousService.deleteById(id);
        return ResponseEntity.ok("Rendez-vous deleted successfully");
    }

    @GetMapping("/medecin/{medecinId}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE') or (hasRole('MEDECIN') and @authz.isCurrentUser(#medecinId))")
    public ResponseEntity<List<RendezVousResponseDTO>> getRendezVousByMedecin(@PathVariable Long medecinId) {
        List<RendezVousResponseDTO> rendezVousList = rendezVousService.findByMedecinId(medecinId);
        return ResponseEntity.ok(rendezVousList);
    }

    // Variante paginée (?page=0&size=20&sort=...) ; sans "page", la liste complète ci-dessus reste servie
    @GetMapping(value = "/medecin/{medecinId}", params = "page")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE') or (hasRole('MEDECIN') and @authz.isCurrentUser(#medecinId))")
    public ResponseEntity<PageResponse<RendezVousResponseDTO>> getRendezVousByMedecinPage(@PathVariable Long medecinId, @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(rendezVousService.findByMedecinId(medecinId, pageable));
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE') or (hasRole('MEDECIN') and @authz.ownsRendezVous(#id))")
    public ResponseEntity<RendezVousResponseDTO> updateStatutRendezVous(@PathVariable Long id, @RequestParam("statut") StatutRendezVous statut) {
        RendezVousResponseDTO updatedRendezVous = rendezVousService.updateStatut(id, statut);
        return ResponseEntity.ok(updatedRendezVous);
    }

    // "/aujourdhui" (sans apostrophe) est la route à utiliser ; "/aujourd'hui" est conservée pour compatibilité
    @GetMapping({"/medecin/{medecinId}/aujourdhui", "/medecin/{medecinId}/aujourd'hui"})
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE') or (hasRole('MEDECIN') and @authz.isCurrentUser(#medecinId))")
    public ResponseEntity<List<RendezVousResponseDTO>> getRendezVousDuJourByMedecin(@PathVariable Long medecinId) {
        List<RendezVousResponseDTO> rendezVousList = rendezVousService.findRendezVousDuJourByMedecin(medecinId);
        return ResponseEntity.ok(rendezVousList);
    }
}
