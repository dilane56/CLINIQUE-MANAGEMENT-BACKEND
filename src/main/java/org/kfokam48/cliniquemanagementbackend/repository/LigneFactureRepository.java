package org.kfokam48.cliniquemanagementbackend.repository;

import org.kfokam48.cliniquemanagementbackend.model.LigneFacture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LigneFactureRepository extends JpaRepository<LigneFacture, Long> {
    // Contrôle de propriété (@authz) : cette ligne appartient-elle à une facture de ce médecin ?
    boolean existsByIdAndFacture_RendezVous_Medecin_Id(Long id, Long medecinId);
}

