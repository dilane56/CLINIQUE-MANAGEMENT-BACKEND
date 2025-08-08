package org.kfokam48.cliniquemanagementbackend.controlleur;

import com.itextpdf.text.DocumentException;
import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureDTO;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureResponseDto;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FacturePaiementUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.model.Facture;
import org.kfokam48.cliniquemanagementbackend.repository.FactureRepository;
import org.kfokam48.cliniquemanagementbackend.service.impl.FactureServiceImpl;
import org.kfokam48.cliniquemanagementbackend.service.pdf.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;

@RestController
@RequestMapping("/api/factures")
public class FactureController {
    private final FactureServiceImpl factureService;
    private final PdfService pdfService;
    private final FactureRepository factureRepository;

    public FactureController(FactureServiceImpl factureService, PdfService pdfService, FactureRepository factureRepository) {
        this.factureService = factureService;
        this.pdfService = pdfService;
        this.factureRepository = factureRepository;
    }
    @PostMapping()
    public ResponseEntity<FactureResponseDto> createFacture(@Valid @RequestBody FactureDTO factureDTO) {
        FactureResponseDto facture = factureService.save(factureDTO);
        return ResponseEntity.ok(facture);
    }

    @GetMapping()
    public ResponseEntity<List<FactureResponseDto>> getAllFactures() {
        List<FactureResponseDto> factures = factureService.findAll();
        return ResponseEntity.ok(factures);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FactureResponseDto> getFactureById(@PathVariable Long id) {
        FactureResponseDto facture = factureService.findById(id);
        return ResponseEntity.ok(facture);
    }

    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<List<FactureResponseDto>> getFacturesByMedecin(@PathVariable Long medecinId) {
        List<FactureResponseDto> factures = factureService.findByMedecinId(medecinId);
        return ResponseEntity.ok(factures);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FactureResponseDto> updateFacture(@PathVariable Long id,@Valid @RequestBody FactureDTO factureDTO) {
        FactureResponseDto updatedFacture = factureService.update(id, factureDTO);
        return ResponseEntity.ok(updatedFacture);
    }

    @PutMapping("/{id}/paiement")
    public ResponseEntity<FactureResponseDto> updatePaiementFacture(@PathVariable Long id, @Valid @RequestBody FacturePaiementUpdateDTO paiementUpdateDTO) {
        FactureResponseDto updatedFacture = factureService.updatePaiement(id, paiementUpdateDTO);
        return ResponseEntity.ok(updatedFacture);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFacture(@PathVariable Long id) {
        return factureService.deleteById(id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generateFacturePdf(@PathVariable Long id) throws DocumentException {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Facture not found with id: " + id));
        if (facture == null) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayOutputStream pdfOutputStream = pdfService.generateFacturePdf(facture);
        byte[] pdfBytes = pdfOutputStream.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=facture_" + facture.getId() + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

}
