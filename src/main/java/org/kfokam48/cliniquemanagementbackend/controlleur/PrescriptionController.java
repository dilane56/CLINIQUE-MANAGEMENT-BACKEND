package org.kfokam48.cliniquemanagementbackend.controlleur;
import org.springframework.security.access.prepost.PreAuthorize;

import com.itextpdf.text.DocumentException;
import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionDTO;
import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.model.Prescription;
import org.kfokam48.cliniquemanagementbackend.repository.PrescriptionRepository;
import org.kfokam48.cliniquemanagementbackend.service.impl.PrescriptionServiceImpl;
import org.kfokam48.cliniquemanagementbackend.service.pdf.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionServiceImpl prescriptionService;
    private final PdfService pdfService;
    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionController(PrescriptionServiceImpl prescriptionService, PdfService pdfService, PrescriptionRepository prescriptionRepository) {
        this.prescriptionService = prescriptionService;
        this.pdfService = pdfService;

        this.prescriptionRepository = prescriptionRepository;
    }


    // Endpoint pour créer une nouvelle prescription
    @PostMapping
    @PreAuthorize("hasRole('MEDECIN')")
    public ResponseEntity<PrescriptionResponseDTO> createPrescription(@RequestBody @Valid PrescriptionDTO prescriptionDTO) {
        PrescriptionResponseDTO prescription = prescriptionService.save(prescriptionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(prescription);
    }

    // Endpoint pour récupérer une prescription par son ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE','MEDECIN')")
    public ResponseEntity<PrescriptionResponseDTO> getPrescriptionById(@PathVariable Long id) {
        PrescriptionResponseDTO prescription = prescriptionService.findById(id);
        return ResponseEntity.ok(prescription);
    }

    // Endpoint pour mettre à jour une prescription existante
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MEDECIN')")
    public ResponseEntity<PrescriptionResponseDTO> updatePrescription(@PathVariable Long id, @RequestBody @Valid PrescriptionUpdateDTO prescriptionDTO) {
        PrescriptionResponseDTO updatedPrescription = prescriptionService.update(id, prescriptionDTO);
        return ResponseEntity.ok(updatedPrescription);
    }

    // Endpoint pour récupérer toutes les prescriptions
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public ResponseEntity<List<PrescriptionResponseDTO>> getAllPrescriptions() {
        List<PrescriptionResponseDTO> prescriptions = prescriptionService.findAll();
        return ResponseEntity.ok(prescriptions);
    }

    // Endpoint pour supprimer une prescription par son ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN')")
    public ResponseEntity<String> deletePrescription(@PathVariable Long id) {
        return prescriptionService.deleteById(id);
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE','MEDECIN')")
    public ResponseEntity<byte[]> generatePrescriptionPdf(@PathVariable Long id) throws DocumentException, FileNotFoundException {
        Prescription prescription = prescriptionRepository.findById(id).orElseThrow(() -> new RessourceNotFoundException("Prescription not found"));
        if (prescription == null) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayOutputStream pdfOutputStream = pdfService.generatePrescriptionPdf(prescription);
        byte[] pdfBytes = pdfOutputStream.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=prescription_" + prescription.getId() + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    // Endpoint pour récupérer toutes les prescriptions d'un médecin spécifique
    @GetMapping("/medecin/{medecinId}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE') or (hasRole('MEDECIN') and @authz.isCurrentUser(#medecinId))")
    public ResponseEntity<List<PrescriptionResponseDTO>> getPrescriptionsByMedecinId(@PathVariable Long medecinId) {
        List<PrescriptionResponseDTO> prescriptions = prescriptionService.findByMedecinId(medecinId);
        return ResponseEntity.ok(prescriptions);
    }
}
