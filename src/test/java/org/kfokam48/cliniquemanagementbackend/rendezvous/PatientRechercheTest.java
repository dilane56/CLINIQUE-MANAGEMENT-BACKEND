package org.kfokam48.cliniquemanagementbackend.rendezvous;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.kfokam48.cliniquemanagementbackend.repository.PatientRepository;
import org.kfokam48.cliniquemanagementbackend.repository.PatientSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Recherche de patients (GET /api/patients/recherche) : choix du patient d'un rendez-vous sans
 * charger toute la liste. Exécutée sur H2 (et sur PostgreSQL en CI via PostgreSqlIntegrationTest).
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:recherchepatients;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        // Indépendant du profil actif (la CI utilise "prod", qui impose le dialecte PostgreSQL)
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class PatientRechercheTest {

    private static final PageRequest PREMIERE_PAGE = PageRequest.of(0, 10, Sort.by("nom", "prenom"));

    @Autowired private TestEntityManager em;
    @Autowired private PatientRepository repository;

    private Patient aicha;
    private Patient jean;
    private Patient marie;

    private Patient patient(String prenom, String nom, String email, String telephone) {
        Patient patient = RendezVousTestData.patient(email);
        patient.setPrenom(prenom);
        patient.setNom(nom);
        patient.setTelephone(telephone);
        return em.persist(patient);
    }

    @BeforeEach
    void setUp() {
        aicha = patient("Aïcha", "Ngono", "aicha@mail.cm", "677112233");
        jean = patient("Jean", "Fotso", "jean.fotso@mail.cm", "655443322");
        marie = patient("Marie", "Fotso", "marie_f@mail.cm", "699000111");
        em.flush();
    }

    private List<Patient> chercher(String texte) {
        return repository.findAll(PatientSpecifications.rechercher(texte), PREMIERE_PAGE).getContent();
    }

    @Test
    void emptySearchReturnsEveryPatientSortedByName() {
        assertThat(chercher(null)).containsExactly(jean, marie, aicha);
        assertThat(chercher("  ")).hasSize(3);
    }

    @Test
    void searchesNameInBothOrdersCaseInsensitive() {
        assertThat(chercher("FOTSO")).containsExactly(jean, marie);
        assertThat(chercher("jean fotso")).containsExactly(jean);
        assertThat(chercher("fotso marie")).containsExactly(marie);
    }

    @Test
    void searchesEmailAndPhone() {
        assertThat(chercher("aicha@")).containsExactly(aicha);
        assertThat(chercher("65544")).containsExactly(jean);
    }

    @Test
    void wildcardCharactersAreSearchedLiterally() {
        // "_" n'est pas un joker : seul l'e-mail de Marie contient un vrai "_"
        assertThat(chercher("_")).containsExactly(marie);
        assertThat(chercher("%")).isEmpty();
    }

    @Test
    void resultsArePaginated() {
        Page<Patient> page = repository.findAll(PatientSpecifications.rechercher("fotso"),
                PageRequest.of(0, 1, Sort.by("nom", "prenom")));
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).containsExactly(jean);
    }
}
