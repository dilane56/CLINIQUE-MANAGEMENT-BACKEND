package org.kfokam48.cliniquemanagementbackend.rendezvous;

import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.dto.RevenuMedecinDTO;
import org.kfokam48.cliniquemanagementbackend.enums.StatutFacture;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.model.*;
import org.kfokam48.cliniquemanagementbackend.repository.PaiementRepository;
import org.kfokam48.cliniquemanagementbackend.service.RevenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kfokam48.cliniquemanagementbackend.rendezvous.RendezVousTestData.*;

/**
 * I22 : une facture de 15 000 FCFA (consultation 10 000 + pansement 5 000) payée en deux tranches,
 * 5 000 en janvier puis 10 000 en février. Chaque tranche doit compter dans son propre mois.
 * (Avant : janvier retombait à 0 et février affichait 15 000.) Requêtes exécutées sur H2.
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:revenus;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        // Indépendant du profil actif (la CI utilise "prod", qui impose le dialecte PostgreSQL)
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@Import(RevenuService.class)
class RevenusPaiementsParTrancheTest {

    private static final LocalDate JANVIER = LocalDate.of(2030, 1, 1);
    private static final LocalDate FEVRIER = LocalDate.of(2030, 2, 1);

    @Autowired private TestEntityManager em;
    @Autowired private PaiementRepository paiementRepository;
    @Autowired private RevenuService revenuService;

    private Medecin factureEnDeuxTranches() {
        Medecin medecin = em.persist(medecin("medecin@test.com"));
        medecin.setNom("Martin");
        Patient patient = em.persist(patient("patient@test.com"));
        TypeRendezVous type = em.persist(consultation30Minutes());
        RendezVous rendezVous = em.persist(rendezVous(medecin, patient, type,
                LocalDateTime.of(2030, 1, 10, 9, 0), StatutRendezVous.TERMINE));

        Facture facture = new Facture();
        facture.setRendezVous(rendezVous);
        facture.setStatut(StatutFacture.PAYEE);
        facture.setMontantTotal(new BigDecimal("15000.00"));
        facture.setMontantPayement(new BigDecimal("15000.00"));
        facture.setMontantRestant(BigDecimal.ZERO);
        facture.getLignes().add(ligne(facture, "Consultation générale", "10000.00"));
        facture.getLignes().add(ligne(facture, "Pansement", "5000.00"));
        em.persist(facture);

        em.persist(new Paiement(facture, new BigDecimal("5000.00"), LocalDateTime.of(2030, 1, 10, 9, 45)));
        em.persist(new Paiement(facture, new BigDecimal("10000.00"), LocalDateTime.of(2030, 2, 3, 14, 0)));
        em.flush();
        return medecin;
    }

    private static LigneFacture ligne(Facture facture, String service, String montant) {
        LigneFacture ligne = new LigneFacture();
        ligne.setFacture(facture);
        ligne.setServiceName(service);
        ligne.setQuantite(1);
        ligne.setPrixUnitaire(new BigDecimal(montant));
        ligne.setPrixTotal(new BigDecimal(montant));
        return ligne;
    }

    @Test
    void eachInstallmentCountsInItsOwnMonth() {
        factureEnDeuxTranches();

        assertThat(paiementRepository.sommeEncaisseeEntre(JANVIER.atStartOfDay(), FEVRIER.atStartOfDay()))
                .isEqualByComparingTo("5000");
        assertThat(paiementRepository.sommeEncaisseeEntre(FEVRIER.atStartOfDay(), FEVRIER.plusMonths(1).atStartOfDay()))
                .isEqualByComparingTo("10000");
    }

    @Test
    void revenueByMedecinAndServiceForJanuary() {
        Medecin medecin = factureEnDeuxTranches();

        List<RevenuMedecinDTO> janvier = revenuService.getRevenusParMedecin(JANVIER, LocalDate.of(2030, 1, 31));

        assertThat(janvier).hasSize(1);
        RevenuMedecinDTO revenu = janvier.get(0);
        assertThat(revenu.medecinId()).isEqualTo(medecin.getId());
        assertThat(revenu.nom()).isEqualTo("Martin");
        assertThat(revenu.totalEncaisse()).isEqualByComparingTo("5000.00");
        // 5 000 répartis au prorata : 2/3 consultation, 1/3 pansement
        assertThat(revenu.services()).containsExactly(
                new RevenuMedecinDTO.RevenuParService("Consultation générale", new BigDecimal("3333.33")),
                new RevenuMedecinDTO.RevenuParService("Pansement", new BigDecimal("1666.67")));
    }

    @Test
    void wholePeriodGivesTheFullInvoice() {
        factureEnDeuxTranches();
        List<RevenuMedecinDTO> deuxMois = revenuService.getRevenusParMedecin(JANVIER, LocalDate.of(2030, 2, 28));
        assertThat(deuxMois.get(0).totalEncaisse()).isEqualByComparingTo("15000.00");
        assertThat(deuxMois.get(0).services()).extracting(RevenuMedecinDTO.RevenuParService::montantEncaisse)
                .containsExactly(new BigDecimal("10000.00"), new BigDecimal("5000.00"));
    }
}
