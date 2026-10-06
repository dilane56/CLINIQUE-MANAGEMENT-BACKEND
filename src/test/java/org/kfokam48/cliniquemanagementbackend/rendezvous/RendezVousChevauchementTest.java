package org.kfokam48.cliniquemanagementbackend.rendezvous;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.kfokam48.cliniquemanagementbackend.model.TypeRendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.RendezVousRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kfokam48.cliniquemanagementbackend.rendezvous.RendezVousTestData.*;

/**
 * I10 : détection des chevauchements de créneaux (requêtes JPQL exécutées sur H2).
 * Rendez-vous existant du médecin : 10h00 - 10h30.
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chevauchement;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RendezVousChevauchementTest {

    private static final Set<StatutRendezVous> LIBRES =
            EnumSet.of(StatutRendezVous.ANNULER, StatutRendezVous.A_REPROGRAMMER, StatutRendezVous.EXPIRE);
    private static final LocalDateTime DIX_HEURES = LocalDateTime.of(2030, 1, 15, 10, 0);
    private static final Long AUCUN = 0L;

    @Autowired private TestEntityManager em;
    @Autowired private RendezVousRepository repository;

    private Medecin medecin;
    private Patient patient;
    private TypeRendezVous consultation;
    private RendezVous existant;

    @BeforeEach
    void setUp() {
        medecin = em.persist(medecin("medecin@test.com"));
        patient = em.persist(patient("patient@test.com"));
        consultation = em.persist(consultation30Minutes());
        existant = em.persist(rendezVous(medecin, patient, consultation, DIX_HEURES, StatutRendezVous.CONFIRME));
        em.flush();
    }

    private boolean chevauche(LocalDateTime debut, LocalDateTime fin, Long ignorer) {
        return repository.existsChevauchementMedecin(medecin.getId(), debut, fin, ignorer, LIBRES);
    }

    @Test
    void appointmentStartingInsideAnExistingOneOverlaps() {
        // 10h15 - 10h45 : refusé (l'ancienne requête ne testait que le début de l'existant)
        assertThat(chevauche(DIX_HEURES.plusMinutes(15), DIX_HEURES.plusMinutes(45), AUCUN)).isTrue();
    }

    @Test
    void appointmentStartingBeforeAndEndingInsideOverlaps() {
        // 9h45 - 10h15
        assertThat(chevauche(DIX_HEURES.minusMinutes(15), DIX_HEURES.plusMinutes(15), AUCUN)).isTrue();
    }

    @Test
    void appointmentCoveringAnExistingOneOverlaps() {
        // 9h30 - 11h00
        assertThat(chevauche(DIX_HEURES.minusMinutes(30), DIX_HEURES.plusHours(1), AUCUN)).isTrue();
    }

    @Test
    void backToBackAppointmentsDoNotOverlap() {
        // 10h30 - 11h00 et 9h30 - 10h00 : acceptés (avant, la borne incluse les refusait)
        assertThat(chevauche(DIX_HEURES.plusMinutes(30), DIX_HEURES.plusHours(1), AUCUN)).isFalse();
        assertThat(chevauche(DIX_HEURES.minusMinutes(30), DIX_HEURES, AUCUN)).isFalse();
    }

    @Test
    void cancelledAppointmentFreesTheSlot() {
        existant.setStatutRendezVous(StatutRendezVous.ANNULER);
        em.flush();
        assertThat(chevauche(DIX_HEURES, DIX_HEURES.plusMinutes(30), AUCUN)).isFalse();
    }

    @Test
    void appointmentBeingUpdatedIsIgnored() {
        assertThat(chevauche(DIX_HEURES.plusMinutes(10), DIX_HEURES.plusMinutes(40), existant.getId())).isFalse();
    }

    @Test
    void otherMedecinIsNotAffected() {
        Medecin autre = em.persist(medecin("autre@test.com"));
        em.flush();
        assertThat(repository.existsChevauchementMedecin(autre.getId(), DIX_HEURES, DIX_HEURES.plusMinutes(30), AUCUN, LIBRES))
                .isFalse();
    }

    @Test
    void patientAlreadyBookedThatDay() {
        LocalDateTime debutJour = DIX_HEURES.toLocalDate().atStartOfDay();
        assertThat(repository.existsRendezVousPatientSurPeriode(
                patient.getId(), debutJour, debutJour.plusDays(1), AUCUN, LIBRES)).isTrue();
        assertThat(repository.existsRendezVousPatientSurPeriode(
                patient.getId(), debutJour.plusDays(1), debutJour.plusDays(2), AUCUN, LIBRES)).isFalse();

        existant.setStatutRendezVous(StatutRendezVous.ANNULER);
        em.flush();
        assertThat(repository.existsRendezVousPatientSurPeriode(
                patient.getId(), debutJour, debutJour.plusDays(1), AUCUN, LIBRES)).isFalse();
    }
}
