package org.kfokam48.cliniquemanagementbackend.controlleur;


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
@RequestMapping("/api/patient")
public class PatientController {
    private final PatientServiceImpl patientService;

    public PatientController(PatientServiceImpl patientService) {
        this.patientService = patientService;
    }
    @PostMapping("/create")
    @PreAuthorize("hasRole('MEDECIN') or hasRole('ADMIN') or hasRole('SECRETAIRE')") // Accès pour les rôles MEDECIN, ADMIN et SECRETAIRE
    public ResponseEntity<PatientResponseDTO> createPatient(@Valid @RequestBody PatientDTO patientDto) {
        PatientResponseDTO patient = patientService.save(patientDto);
        return ResponseEntity.ok(patient);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('MEDECIN') or hasRole('ADMIN') or hasRole('SECRETAIRE')") // Accès pour les rôles MEDECIN, ADMIN et SECRETAIRE
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients() {
        List<PatientResponseDTO> patients = patientService.findAll();
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MEDECIN') or hasRole('ADMIN') or hasRole('SECRETAIRE')  or hasRole('PATIENT')") // Accès pour les rôles MEDECIN, ADMIN et SECRETAIRE et PATIENT
    public ResponseEntity<PatientResponseDTO> getPatientById(@PathVariable Long id) {
        PatientResponseDTO patient = patientService.findById(id);
        return ResponseEntity.ok(patient);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('MEDECIN') or hasRole('ADMIN') or hasRole('SECRETAIRE') or hasRole('PATIENT')") // Accès pour les rôles MEDECIN, ADMIN et SECRETAIRE et PATIENT
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable Long id,@Valid @RequestBody PatientDTO patientDto) {
        PatientResponseDTO updatedPatient = patientService.update(id, patientDto);
        return ResponseEntity.ok(updatedPatient);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('MEDECIN') or hasRole('ADMIN') or hasRole('SECRETAIRE')") // Accès pour les rôles MEDECIN, ADMIN et SECRETAIRE
    public ResponseEntity<String> deletePatient(@PathVariable Long id) {
        ResponseEntity<String> response = patientService.deleteById(id);
        return response;
    }


}
