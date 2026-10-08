package org.kfokam48.cliniquemanagementbackend.rendezvous;

import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;
import org.kfokam48.cliniquemanagementbackend.enums.StatutFacture;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.model.*;
import org.kfokam48.cliniquemanagementbackend.repository.FactureRepository;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.cliniquemanagementbackend.service.Compteurs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kfokam48.cliniquemanagementbackend.rendezvous.RendezVousTestData.*;

/**
 * Compteurs des tableaux de bord, calculés en base (GROUP BY) au lieu de charger toutes les lignes :
 * factures par statut (GET /api/factures/statistiques) et comptes par rôle (GET /api/utilisateurs/statistiques).
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:compteurs;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        // Indépendant du profil actif (la CI utilise "prod", qui impose le dialecte PostgreSQL)
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class CompteursTableauDeBordTest {

    @Autowired private TestEntityManager em;
    @Autowired private FactureRepository factureRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;

    private Facture facture(RendezVous rendezVous, StatutFacture statut) {
        Facture facture = new Facture();
        facture.setRendezVous(rendezVous);
        facture.setMontantTotal(new BigDecimal("15000.00"));
        facture.setMontantRestant(new BigDecimal("15000.00"));
        facture.setDateEmission(LocalDateTime.of(2030, 1, 15, 10, 0));
        facture.setStatut(statut);
        return em.persist(facture);
    }

    @Test
    void countsInvoicesByStatusIncludingMissingOnes() {
        Medecin medecin = em.persist(medecin("martin@test.com"));
        Patient patient = em.persist(patient("aicha@test.com"));
        TypeRendezVous type = em.persist(consultation30Minutes());
        LocalDateTime debut = LocalDateTime.of(2030, 1, 15, 9, 0);
        for (StatutFacture statut : new StatutFacture[]{StatutFacture.NON_PAYEE, StatutFacture.NON_PAYEE,
                StatutFacture.PARTIELLEMENT_PAYE, StatutFacture.PAYEE}) {
            RendezVous rendezVous = em.persist(rendezVous(medecin, patient, type, debut, StatutRendezVous.TERMINE));
            facture(rendezVous, statut);
            debut = debut.plusHours(1);
        }
        em.flush();

        Map<StatutFacture, Long> compteurs = Compteurs.parValeur(StatutFacture.class, factureRepository.compterParStatut());

        assertThat(compteurs).containsExactly(
                Map.entry(StatutFacture.NON_PAYEE, 2L),
                Map.entry(StatutFacture.PAYEE, 1L),
                Map.entry(StatutFacture.PARTIELLEMENT_PAYE, 1L),
                Map.entry(StatutFacture.ANNULEE, 0L));
    }

    @Test
    void countsAccountsByRole() {
        em.persist(medecin("martin@test.com"));
        em.persist(medecin("durand@test.com"));
        Secretaire secretaire = new Secretaire();
        secretaire.setEmail("secretaire@test.com");
        secretaire.setPassword("hash");
        secretaire.setRole(Roles.SECRETAIRE);
        em.persist(secretaire);
        em.flush();

        Map<Roles, Long> compteurs = Compteurs.parValeur(Roles.class, utilisateurRepository.compterParRole());

        assertThat(compteurs).containsExactly(
                Map.entry(Roles.MEDECIN, 2L),
                Map.entry(Roles.ADMIN, 0L),
                Map.entry(Roles.SECRETAIRE, 1L));
    }
}
