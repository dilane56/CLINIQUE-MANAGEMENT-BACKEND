package org.kfokam48.cliniquemanagementbackend.service;

import org.kfokam48.cliniquemanagementbackend.dto.patient.PatientDTO;
import org.kfokam48.cliniquemanagementbackend.dto.patient.PatientResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.springframework.http.ResponseEntity;

import java.util.List;


public interface PatientService {
    PatientResponseDTO save(PatientDTO patientDto);
    PatientResponseDTO findById(Long id);
    PatientResponseDTO update(Long id, PatientDTO patientDTO);
    ResponseEntity<String > deleteById(Long id);
    List<PatientResponseDTO> findAll();
}
