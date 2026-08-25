package org.kfokam48.cliniquemanagementbackend.controlleur;

import com.itextpdf.text.DocumentException;
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
    public ResponseEntity<FactureResponseDto> createFacture(@Valid @RequestBody FactureDTO factureDTO) {
        return ResponseEntity.ok(factureService.save(factureDTO));
    }

    @GetMapping
    public ResponseEntity<List<FactureResponseDto>> getAllFactures() {
        return ResponseEntity.ok(factureService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FactureResponseDto> getFactureById(@PathVariable Long id) {
        return ResponseEntity.ok(factureService.findById(id));
    }

    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<List<FactureResponseDto>> getFacturesByMedecin(@PathVariable Long medecinId) {
        return ResponseEntity.ok(factureService.findByMedecinId(medecinId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FactureResponseDto> updateFacture(@PathVariable Long id, @Valid @RequestBody FactureDTO factureDTO) {
        return ResponseEntity.ok(factureService.update(id, factureDTO));
    }

    @PutMapping("/{id}/paiement")
    public ResponseEntity<FactureResponseDto> updatePaiementFacture(@PathVariable Long id, @Valid @RequestBody FacturePaiementUpdateDTO paiementUpdateDTO) {
        return ResponseEntity.ok(factureService.updatePaiement(id, paiementUpdateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFacture(@PathVariable Long id) {
        return factureService.deleteById(id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generateFacturePdf(@PathVariable Long id) throws DocumentException {
        ByteArrayOutputStream pdfOutputStream = factureService.generatePdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=facture_" + id + ".pdf");
        return new ResponseEntity<>(pdfOutputStream.toByteArray(), headers, HttpStatus.OK);
    }
}
