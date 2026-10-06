package org.kfokam48.cliniquemanagementbackend.service;


import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousUpdateDto;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;

import java.util.List;

public interface RendezVousService {

    RendezVousResponseDTO save(RendezVousDTO rendezVousDTO);
    RendezVousResponseDTO findById(Long Id);
    RendezVousResponseDTO update(Long id, RendezVousUpdateDto rendezVousDTO);
    List<RendezVousResponseDTO> findAll();
    void deleteById(Long id);
    List<RendezVousResponseDTO> findByMedecinId(Long medecinId);
    RendezVousResponseDTO updateStatut(Long id, StatutRendezVous statut);
    List<RendezVousResponseDTO> findRendezVousDuJourByMedecin(Long medecinId);
    PageResponse<RendezVousResponseDTO> findAll(Pageable pageable);
    PageResponse<RendezVousResponseDTO> findByMedecinId(Long medecinId, Pageable pageable);
}
