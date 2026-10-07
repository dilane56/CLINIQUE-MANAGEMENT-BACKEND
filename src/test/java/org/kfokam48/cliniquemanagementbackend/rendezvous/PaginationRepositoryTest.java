package org.kfokam48.cliniquemanagementbackend.rendezvous;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.kfokam48.cliniquemanagementbackend.model.TypeRendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.PatientRepository;
import org.kfokam48.cliniquemanagementbackend.repository.RendezVousRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyReferenceException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.kfokam48.cliniquemanagementbackend.rendezvous.RendezVousTestData.*;

/**
 * I11 : requêtes paginées (exécutées sur H2).
 * Le médecin a 3 rendez-vous avec 2 patients distincts ; un autre médecin a un patient à lui.
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:pagination;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        // Indépendant du profil actif (la CI utilise "prod", qui impose le dialecte PostgreSQL)
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class PaginationRepositoryTest {

    private static final LocalDateTime JOUR = LocalDateTime.of(2030, 3, 10, 9, 0);

    @Autowired private TestEntityManager em;
    @Autowired private PatientRepository patientRepository;
    @Autowired private RendezVousRepository rendezVousRepository;

    private Medecin medecin;
    private Patient alice;
    private Patient bruno;

    @BeforeEach
    void setUp() {
        medecin = em.persist(medecin("medecin@test.com"));
        Medecin autreMedecin = em.persist(medecin("autre@test.com"));
        alice = em.persist(patient("alice@test.com"));
        bruno = em.persist(patient("bruno@test.com"));
        Patient claire = em.persist(patient("claire@test.com"));
        TypeRendezVous type = em.persist(consultation30Minutes());

        // Alice vient deux fois : elle ne doit apparaître qu'une fois dans les patients du médecin
        em.persist(rendezVous(medecin, alice, type, JOUR, StatutRendezVous.TERMINE));
        em.persist(rendezVous(medecin, alice, type, JOUR.plusDays(7), StatutRendezVous.CONFIRME));
        em.persist(rendezVous(medecin, bruno, type, JOUR.plusHours(1), StatutRendezVous.CONFIRME));
        em.persist(rendezVous(autreMedecin, claire, type, JOUR, StatutRendezVous.CONFIRME));
        em.flush();
    }

    @Test
    void patientsOfAMedecinArePaginatedWithoutDuplicates() {
        Page<Patient> premiere = patientRepository.findPatientsByMedecinId(medecin.getId(),
                PageRequest.of(0, 1, Sort.by("id")));
        Page<Patient> seconde = patientRepository.findPatientsByMedecinId(medecin.getId(),
                PageRequest.of(1, 1, Sort.by("id")));

        assertThat(premiere.getTotalElements()).isEqualTo(2);
        assertThat(premiere.getTotalPages()).isEqualTo(2);
        assertThat(premiere.getContent()).containsExactly(alice);
        assertThat(seconde.getContent()).containsExactly(bruno);
    }

    @Test
    void appointmentsOfAMedecinArePaginated() {
        Page<?> page = rendezVousRepository.findByMedecinId(medecin.getId(), PageRequest.of(0, 2, Sort.by("id")));
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void sortingOnAnUnknownPropertyIsRejected() {
        // Transformé en 400 par GlobalExceptionHandler
        assertThatThrownBy(() -> patientRepository.findAll(PageRequest.of(0, 10, Sort.by("inconnu"))))
                .isInstanceOf(PropertyReferenceException.class);
    }
}
