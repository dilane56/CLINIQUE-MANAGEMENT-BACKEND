package org.kfokam48.cliniquemanagementbackend.repository;

import org.kfokam48.cliniquemanagementbackend.model.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    // Total encaissé sur la période [debut, fin[
    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM Paiement p WHERE p.datePaiement >= :debut AND p.datePaiement < :fin")
    BigDecimal sommeEncaisseeEntre(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    /**
     * Une ligne par (paiement, ligne de facture) sur la période [debut, fin[ : sert à répartir chaque
     * paiement entre les services de sa facture, au prorata de leur montant (calcul fait en Java).
     */
    @Query("""
            SELECT m.id AS medecinId, m.nom AS medecinNom, m.prenom AS medecinPrenom,
                   p.id AS paiementId, p.montant AS montantPaiement,
                   l.serviceName AS service, l.prixTotal AS montantLigne, f.montantTotal AS montantFacture
            FROM Paiement p
            JOIN p.facture f
            JOIN f.lignes l
            JOIN f.rendezVous r
            JOIN r.medecin m
            WHERE p.datePaiement >= :debut AND p.datePaiement < :fin
            """)
    List<LignePaiementParService> lignesPaiementParService(@Param("debut") LocalDateTime debut,
                                                           @Param("fin") LocalDateTime fin);

    interface LignePaiementParService {
        Long getMedecinId();
        String getMedecinNom();
        String getMedecinPrenom();
        Long getPaiementId();
        BigDecimal getMontantPaiement();
        String getService();
        BigDecimal getMontantLigne();
        BigDecimal getMontantFacture();
    }
}
