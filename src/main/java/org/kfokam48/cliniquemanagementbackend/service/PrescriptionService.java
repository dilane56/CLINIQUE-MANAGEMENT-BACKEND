package org.kfokam48.cliniquemanagementbackend.service;


import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionDTO;
import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionUpdateDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;


public interface PrescriptionService {
    PrescriptionResponseDTO save(PrescriptionDTO prescriptionDTO);
   PrescriptionResponseDTO findById(Long id);
    PrescriptionResponseDTO update(Long id , PrescriptionUpdateDTO prescriptionDTO);
    List<PrescriptionResponseDTO> findAll();
    List<PrescriptionResponseDTO> findByMedecinId(Long medecinId);
    ResponseEntity<String > deleteById(Long id);
}
