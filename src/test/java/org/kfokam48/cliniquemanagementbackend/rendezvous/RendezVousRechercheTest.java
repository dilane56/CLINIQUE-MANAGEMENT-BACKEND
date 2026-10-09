package org.kfokam48.cliniquemanagementbackend.rendezvous;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.CriteresRendezVous;
import org.kfokam48.cliniquemanagementbackend.enums.StatutFacture;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.model.Facture;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.kfokam48.cliniquemanagementbackend.model.TypeRendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.RendezVousRepository;
import org.kfokam48.cliniquemanagementbackend.repository.RendezVousSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kfokam48.cliniquemanagementbackend.rendezvous.RendezVousTestData.*;

/**
 * Recherche paginée des rendez-vous (page Rendez-vous de l'admin), exécutée sur H2.
 * Jeu de données : Dr Paul Martin et Dr Anne Durand ; patients Aïcha Ngono et Jean Fotso.
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:recherche;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        // Indépendant du profil actif (la CI utilise "prod", qui impose le dialecte PostgreSQL)
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class RendezVousRechercheTest {

    private static final LocalDate LE_15 = LocalDate.of(2030, 1, 15);
    private static final PageRequest PREMIERE_PAGE = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "dateRendezVous"));

    @Autowired private TestEntityManager em;
    @Autowired private RendezVousRepository repository;

    private RendezVous aichaChezMartinLe15;
    private RendezVous jeanChezMartinLe15;
    private RendezVous jeanChezDurandLe16;

    private Medecin medecin(String email, String prenom, String nom) {
        Medecin medecin = RendezVousTestData.medecin(email);
        medecin.setPrenom(prenom);
        medecin.setNom(nom);
        return em.persist(medecin);
    }

    private Patient patient(String email, String prenom, String nom) {
        Patient patient = RendezVousTestData.patient(email);
        patient.setPrenom(prenom);
        patient.setNom(nom);
        return em.persist(patient);
    }

    @BeforeEach
    void setUp() {
        Medecin martin = medecin("martin@test.com", "Paul", "Martin");
        Medecin durand = medecin("durand@test.com", "Anne", "Durand");
        Patient aicha = patient("aicha@test.com", "Aïcha", "Ngono");
        Patient jean = patient("jean@test.com", "Jean", "Fotso");
        TypeRendezVous consultation = em.persist(consultation30Minutes());
        TypeRendezVous vaccination = em.persist(new TypeRendezVous(null, "Vaccination", 15, new BigDecimal("10000.00")));

        aichaChezMartinLe15 = em.persist(rendezVous(martin, aicha, consultation, LE_15.atTime(9, 0), StatutRendezVous.CONFIRME));
        jeanChezMartinLe15 = em.persist(rendezVous(martin, jean, vaccination, LE_15.atTime(10, 0), StatutRendezVous.EN_ATTENTE));
        jeanChezDurandLe16 = em.persist(rendezVous(durand, jean, consultation, LE_15.plusDays(1).atTime(23, 30), StatutRendezVous.CONFIRME));
        em.flush();
    }

    private List<RendezVous> chercher(String texte, StatutRendezVous statut, LocalDate date) {
        return repository.findAll(RendezVousSpecifications.rechercher(texte, statut, date), PREMIERE_PAGE).getContent();
    }

    @Test
    void noCriteriaReturnsEverythingMostRecentFirst() {
        assertThat(chercher(null, null, null))
                .containsExactly(jeanChezDurandLe16, jeanChezMartinLe15, aichaChezMartinLe15);
        assertThat(chercher("   ", null, null)).hasSize(3);
    }

    @Test
    void searchesPatientByNameInBothOrdersCaseInsensitive() {
        assertThat(chercher("NGONO", null, null)).containsExactly(aichaChezMartinLe15);
        assertThat(chercher("ngono aïcha", null, null)).containsExactly(aichaChezMartinLe15);
        assertThat(chercher("aïcha ngono", null, null)).containsExactly(aichaChezMartinLe15);
    }

    @Test
    void searchesMedecinAndAppointmentType() {
        assertThat(chercher("durand", null, null)).containsExactly(jeanChezDurandLe16);
        assertThat(chercher("paul martin", null, null)).containsExactly(jeanChezMartinLe15, aichaChezMartinLe15);
        assertThat(chercher("vaccin", null, null)).containsExactly(jeanChezMartinLe15);
    }

    @Test
    void filtersByStatusAndDay() {
        assertThat(chercher(null, StatutRendezVous.CONFIRME, null)).containsExactly(jeanChezDurandLe16, aichaChezMartinLe15);
        assertThat(chercher(null, null, LE_15)).containsExactly(jeanChezMartinLe15, aichaChezMartinLe15);
        // 23h30 le 16 : bien rattaché au 16 et pas au 17
        assertThat(chercher(null, null, LE_15.plusDays(1))).containsExactly(jeanChezDurandLe16);
        assertThat(chercher(null, null, LE_15.plusDays(2))).isEmpty();
    }

    @Test
    void criteriaAreCombined() {
        assertThat(chercher("jean", StatutRendezVous.CONFIRME, null)).containsExactly(jeanChezDurandLe16);
        assertThat(chercher("martin", StatutRendezVous.CONFIRME, LE_15)).containsExactly(aichaChezMartinLe15);
        assertThat(chercher("durand", null, LE_15)).isEmpty();
    }

    private List<RendezVous> chercher(CriteresRendezVous criteres) {
        return repository.findAll(RendezVousSpecifications.rechercher(criteres), PREMIERE_PAGE).getContent();
    }

    @Test
    void filtersByDoctorPeriodAndSeveralStatuses() {
        Long martin = aichaChezMartinLe15.getMedecin().getId();
        assertThat(chercher(new CriteresRendezVous(null, null, null, null, null, martin, null)))
                .containsExactly(jeanChezMartinLe15, aichaChezMartinLe15);
        // Période inclusive, à la journée (le 16 à 23h30 compte dans [15, 16])
        assertThat(chercher(new CriteresRendezVous(null, null, null, LE_15, LE_15.plusDays(1), null, null))).hasSize(3);
        assertThat(chercher(new CriteresRendezVous(null, null, null, LE_15.plusDays(1), null, null, null)))
                .containsExactly(jeanChezDurandLe16);
        assertThat(chercher(new CriteresRendezVous(null, null, null, null, LE_15, null, null)))
                .containsExactly(jeanChezMartinLe15, aichaChezMartinLe15);
        assertThat(chercher(new CriteresRendezVous(null,
                List.of(StatutRendezVous.CONFIRME, StatutRendezVous.EN_ATTENTE), null, null, null, martin, null)))
                .containsExactly(jeanChezMartinLe15, aichaChezMartinLe15);
    }

    @Test
    void excludesAppointmentsThatAlreadyHaveAnInvoice() {
        Facture facture = new Facture();
        facture.setRendezVous(aichaChezMartinLe15);
        facture.setStatut(StatutFacture.NON_PAYEE);
        em.persist(facture);
        em.flush();

        assertThat(chercher(new CriteresRendezVous(null, null, null, null, null, null, true)))
                .containsExactly(jeanChezDurandLe16, jeanChezMartinLe15);
        assertThat(chercher(new CriteresRendezVous(null, null, null, null, null, null, false))).hasSize(3);
    }

    @Test
    void wildcardCharactersAreSearchedLiterally() {
        assertThat(chercher("%", null, null)).isEmpty();
        assertThat(chercher("_", null, null)).isEmpty();
    }

    @Test
    void resultsArePaginated() {
        Page<RendezVous> page = repository.findAll(RendezVousSpecifications.rechercher("jean", null, null),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "dateRendezVous")));
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).containsExactly(jeanChezDurandLe16);
    }

    @Test
    void countsAppointmentsByStatus() {
        assertThat(repository.compterParStatut())
                .extracting(ligne -> ligne[0] + "=" + ligne[1])
                .containsExactlyInAnyOrder("CONFIRME=2", "EN_ATTENTE=1");
    }
}
