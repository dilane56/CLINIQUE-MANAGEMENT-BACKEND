package org.kfokam48.cliniquemanagementbackend.controlleur;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import org.springframework.security.access.prepost.PreAuthorize;

import com.lowagie.text.DocumentException;
import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureDTO;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureResponseDto;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FacturePaiementUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.service.FactureService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;

@RestController
@RequestMapping("/api/factures")
public class FactureController {

    private final FactureService factureService;

    public FactureController(FactureService factureService) {
        this.factureService = factureService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE') or (hasRole('MEDECIN') and @authz.ownsRendezVous(#factureDTO.rendezVousId))")
    public ResponseEntity<FactureResponseDto> createFacture(@Valid @RequestBody FactureDTO factureDTO) {
        return ResponseEntity.ok(factureService.save(factureDTO));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public ResponseEntity<List<FactureResponseDto>> getAllFactures() {
        return ResponseEntity.ok(factureService.findAll());
    }

    // Variante paginée (?page=0&size=20&sort=...) ; sans "page", la liste complète ci-dessus reste servie
    @GetMapping(params = "page")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public ResponseEntity<PageResponse<FactureResponseDto>> getAllFacturesPage(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(factureService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE') or (hasRole('MEDECIN') and @authz.ownsFacture(#id))")
    public ResponseEntity<FactureResponseDto> getFactureById(@PathVariable Long id) {
        return ResponseEntity.ok(factureService.findById(id));
    }

    @GetMapping("/medecin/{medecinId}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE') or (hasRole('MEDECIN') and @authz.isCurrentUser(#medecinId))")
    public ResponseEntity<List<FactureResponseDto>> getFacturesByMedecin(@PathVariable Long medecinId) {
        return ResponseEntity.ok(factureService.findByMedecinId(medecinId));
    }

    // Variante paginée (?page=0&size=20&sort=...) ; sans "page", la liste complète ci-dessus reste servie
    @GetMapping(value = "/medecin/{medecinId}", params = "page")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE') or (hasRole('MEDECIN') and @authz.isCurrentUser(#medecinId))")
    public ResponseEntity<PageResponse<FactureResponseDto>> getFacturesByMedecinPage(@PathVariable Long medecinId, @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(factureService.findByMedecinId(medecinId, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE') or (hasRole('MEDECIN') and @authz.ownsFacture(#id) and @authz.ownsRendezVous(#factureDTO.rendezVousId))")
    public ResponseEntity<FactureResponseDto> updateFacture(@PathVariable Long id, @Valid @RequestBody FactureDTO factureDTO) {
        return ResponseEntity.ok(factureService.update(id, factureDTO));
    }

    @PutMapping("/{id}/paiement")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public ResponseEntity<FactureResponseDto> updatePaiementFacture(@PathVariable Long id, @Valid @RequestBody FacturePaiementUpdateDTO paiementUpdateDTO) {
        return ResponseEntity.ok(factureService.updatePaiement(id, paiementUpdateDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MEDECIN') and @authz.ownsFacture(#id))")
    public ResponseEntity<String> deleteFacture(@PathVariable Long id) {
        factureService.deleteById(id);
        return ResponseEntity.ok("Facture deleted successfully");
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE') or (hasRole('MEDECIN') and @authz.ownsFacture(#id))")
    public ResponseEntity<byte[]> generateFacturePdf(@PathVariable Long id) throws DocumentException {
        ByteArrayOutputStream pdfOutputStream = factureService.generatePdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=facture_" + id + ".pdf");
        return new ResponseEntity<>(pdfOutputStream.toByteArray(), headers, HttpStatus.OK);
    }
}
