package org.kfokam48.cliniquemanagementbackend.service;

import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureDTO;
import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureResponseDTO;

import java.util.List;

public interface LigneFactureService {
    LigneFactureResponseDTO ajouterLigne(LigneFactureDTO dto);
    LigneFactureResponseDTO modifierLigne(Long id, LigneFactureDTO dto);
    void supprimerLigne(Long id);
    LigneFactureResponseDTO getLigne(Long id);
    List<LigneFactureResponseDTO> listerLignes();
}

