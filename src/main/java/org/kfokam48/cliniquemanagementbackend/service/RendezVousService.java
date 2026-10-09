package org.kfokam48.cliniquemanagementbackend.service;


import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.CriteresRendezVous;
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

    // Recherche paginée selon des critères tous facultatifs (texte, statuts, jour, période, médecin, sans facture)
    PageResponse<RendezVousResponseDTO> rechercher(CriteresRendezVous criteres, Pageable pageable);

    // Nombre de rendez-vous par statut (tous les statuts, 0 si aucun)
    Map<StatutRendezVous, Long> compterParStatut();
}
