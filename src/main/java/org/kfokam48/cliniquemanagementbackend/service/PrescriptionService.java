package org.kfokam48.cliniquemanagementbackend.service;


import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionDTO;
import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionUpdateDTO;

import java.util.List;


public interface PrescriptionService {
    PrescriptionResponseDTO save(PrescriptionDTO prescriptionDTO);
   PrescriptionResponseDTO findById(Long id);
    PrescriptionResponseDTO update(Long id , PrescriptionUpdateDTO prescriptionDTO);
    List<PrescriptionResponseDTO> findAll();
    List<PrescriptionResponseDTO> findByMedecinId(Long medecinId);
    void deleteById(Long id);
    PageResponse<PrescriptionResponseDTO> findAll(Pageable pageable);
    PageResponse<PrescriptionResponseDTO> findByMedecinId(Long medecinId, Pageable pageable);
}
