package org.kfokam48.cliniquemanagementbackend.repository;


import org.kfokam48.cliniquemanagementbackend.model.Facture;
import org.kfokam48.cliniquemanagementbackend.enums.StatutFacture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {
    List<Facture> findByRendezVous_Medecin_Id(Long medecinId);

    // Contrôle de propriété (@authz) : cette facture appartient-elle à ce médecin ?
    boolean existsByIdAndRendezVous_Medecin_Id(Long id, Long medecinId);

    @Query("SELECT COALESCE(SUM(f.montantPayement), 0) FROM Facture f WHERE (f.statut = 'PAYEE' OR f.statut = 'PARTIELLEMENT_PAYE') AND MONTH(f.datePayement) = :mois AND YEAR(f.datePayement) = :annee")
    BigDecimal sumRevenuByMoisAndAnnee(@Param("mois") int mois, @Param("annee") int annee);
}
