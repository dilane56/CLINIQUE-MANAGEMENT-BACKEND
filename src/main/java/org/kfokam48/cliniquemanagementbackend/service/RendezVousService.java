package org.kfokam48.cliniquemanagementbackend.service;


import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousResponseDTO;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface RendezVousService {

    RendezVousResponseDTO save(RendezVousDTO rendezVousDTO);
    RendezVousResponseDTO findById(Long Id);
    RendezVousResponseDTO update(Long id, RendezVousDTO rendezVousDTO);
    List<RendezVousResponseDTO> findAll();
    ResponseEntity<String > deleteById(Long id);
    List<RendezVousResponseDTO> findByMedecinId(Long medecinId);
    RendezVousResponseDTO updateStatut(Long id, StatutRendezVous statut);
    List<RendezVousResponseDTO> findRendezVousDuJourByMedecin(Long medecinId);
}
