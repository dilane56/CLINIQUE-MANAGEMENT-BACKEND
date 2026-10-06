package org.kfokam48.cliniquemanagementbackend.service;

import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionUpdateDTO;

import java.util.List;

public interface LignePrescriptionService {
    LignePrescriptionResponseDTO modifierLigne(Long id, LignePrescriptionUpdateDTO dto);
    void supprimerLigne(Long id);
    LignePrescriptionResponseDTO getLigne(Long id);
    List<LignePrescriptionResponseDTO> listerLignes();
}

