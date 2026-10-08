package org.kfokam48.cliniquemanagementbackend.service;

import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import org.kfokam48.cliniquemanagementbackend.dto.patient.PatientDTO;
import org.kfokam48.cliniquemanagementbackend.dto.patient.PatientResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Patient;

import java.util.List;


public interface PatientService {
    PatientResponseDTO save(PatientDTO patientDto);
    PatientResponseDTO findById(Long id);
    PatientResponseDTO update(Long id, PatientDTO patientDTO);
    void deleteById(Long id);
    List<PatientResponseDTO> findAll();
    List<PatientResponseDTO> findByMedecinId(Long medecinId);
    PageResponse<PatientResponseDTO> findAll(Pageable pageable);
    PageResponse<PatientResponseDTO> findByMedecinId(Long medecinId, Pageable pageable);

    // Recherche paginée (nom, prénom, e-mail ou téléphone) ; texte vide : tous les patients
    PageResponse<PatientResponseDTO> rechercher(String texte, Pageable pageable);
}
