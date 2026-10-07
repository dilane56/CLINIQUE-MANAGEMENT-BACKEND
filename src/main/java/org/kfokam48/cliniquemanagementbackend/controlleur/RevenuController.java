package org.kfokam48.cliniquemanagementbackend.controlleur;
import org.springframework.security.access.prepost.PreAuthorize;

import org.kfokam48.cliniquemanagementbackend.dto.RevenuDTO;
import org.kfokam48.cliniquemanagementbackend.dto.RevenuMedecinDTO;
import org.kfokam48.cliniquemanagementbackend.service.RevenuService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/revenus")
public class RevenuController {

    private final RevenuService revenuService;

    public RevenuController(RevenuService revenuService) {
        this.revenuService = revenuService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RevenuDTO> getRevenuMensuel() {
        RevenuDTO revenus = revenuService.getRevenuMensuel();
        return ResponseEntity.ok(revenus);
    }

    /**
     * Revenus encaissés par médecin et par service sur une période (dates ISO, bornes incluses).
     * Par défaut : le mois en cours. Exemple : /api/revenus/medecins?debut=2030-01-01&fin=2030-01-31
     */
    @GetMapping("/medecins")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RevenuMedecinDTO>> getRevenusParMedecin(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        LocalDate debutPeriode = debut != null ? debut : LocalDate.now().withDayOfMonth(1);
        LocalDate finPeriode = fin != null ? fin : debutPeriode.plusMonths(1).minusDays(1);
        return ResponseEntity.ok(revenuService.getRevenusParMedecin(debutPeriode, finPeriode));
    }
}
