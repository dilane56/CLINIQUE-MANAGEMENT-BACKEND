package org.kfokam48.cliniquemanagementbackend.controlleur;


import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.patient.PatientDTO;
import org.kfokam48.cliniquemanagementbackend.dto.patient.PatientResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.kfokam48.cliniquemanagementbackend.service.impl.PatientServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientServiceImpl patientService;

    public PatientController(PatientServiceImpl patientService) {
        this.patientService = patientService;
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    public ResponseEntity<PatientResponseDTO> createPatient(@Valid @RequestBody PatientDTO patientDto) {
        PatientResponseDTO patient = patientService.save(patientDto);
        return ResponseEntity.ok(patient);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE','MEDECIN')")
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients() {
        List<PatientResponseDTO> patients = patientService.findAll();
        return ResponseEntity.ok(patients);
    }

    // Variante paginée (?page=0&size=20&sort=...) ; sans "page", la liste complète ci-dessus reste servie
    @GetMapping(params = "page")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE','MEDECIN')")
    public ResponseEntity<PageResponse<PatientResponseDTO>> getAllPatientsPage(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(patientService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE','MEDECIN')")
    public ResponseEntity<PatientResponseDTO> getPatientById(@PathVariable Long id) {
        PatientResponseDTO patient = patientService.findById(id);
        return ResponseEntity.ok(patient);
    }

    @GetMapping("/medecin/{medecinId}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE') or (hasRole('MEDECIN') and @authz.isCurrentUser(#medecinId))")
    public ResponseEntity<List<PatientResponseDTO>> getPatientsByMedecinId(@PathVariable Long medecinId) {
        List<PatientResponseDTO> patients = patientService.findByMedecinId(medecinId);
        return ResponseEntity.ok(patients);
    }

    // Variante paginée (?page=0&size=20&sort=...) ; sans "page", la liste complète ci-dessus reste servie
    @GetMapping(value = "/medecin/{medecinId}", params = "page")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE') or (hasRole('MEDECIN') and @authz.isCurrentUser(#medecinId))")
    public ResponseEntity<PageResponse<PatientResponseDTO>> getPatientsByMedecinIdPage(@PathVariable Long medecinId, @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(patientService.findByMedecinId(medecinId, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE','MEDECIN')")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable Long id,@Valid @RequestBody PatientDTO patientDto) {
        PatientResponseDTO updatedPatient = patientService.update(id, patientDto);
        return ResponseEntity.ok(updatedPatient);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')") // Un médecin ne peut pas supprimer de patient
    public ResponseEntity<String> deletePatient(@PathVariable Long id) {
        patientService.deleteById(id);
        return ResponseEntity.ok("Patient deleted successfully");
    }


}
