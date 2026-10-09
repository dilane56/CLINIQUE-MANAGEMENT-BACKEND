package org.kfokam48.cliniquemanagementbackend.repository;


import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.kfokam48.cliniquemanagementbackend.model.Facture;
import org.kfokam48.cliniquemanagementbackend.enums.StatutFacture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {
    List<Facture> findByRendezVous_Medecin_Id(Long medecinId);
    Page<Facture> findByRendezVous_Medecin_Id(Long medecinId, Pageable pageable);

    // Contrôle de propriété (@authz) : cette facture appartient-elle à ce médecin ?
    boolean existsByIdAndRendezVous_Medecin_Id(Long id, Long medecinId);

    // Un rendez-vous n'est facturé qu'une fois (à la modification, la facture elle-même est exclue)
    boolean existsByRendezVous_Id(Long rendezVousId);
    boolean existsByRendezVous_IdAndIdNot(Long rendezVousId, Long factureId);


    // Nombre de factures par statut (compteurs du tableau de bord)
    @Query("SELECT f.statut, COUNT(f) FROM Facture f GROUP BY f.statut")
    List<Object[]> compterParStatut();
}
