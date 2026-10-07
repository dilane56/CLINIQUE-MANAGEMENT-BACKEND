package org.kfokam48.cliniquemanagementbackend.migration;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.kfokam48.cliniquemanagementbackend.repository.RendezVousSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.kfokam48.cliniquemanagementbackend.model.TypeRendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.MedecinRepository;
import org.kfokam48.cliniquemanagementbackend.repository.PatientRepository;
import org.kfokam48.cliniquemanagementbackend.repository.RendezVousRepository;
import org.kfokam48.cliniquemanagementbackend.repository.TypeRendezVousRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * I9 / I10 / I21 : vérifications sur un VRAI PostgreSQL (impossibles sur H2).
 * - Flyway applique V1 à V4, et Hibernate valide le schéma (ddl-auto=validate) ;
 * - la contrainte d'exclusion de V2 refuse deux rendez-vous actifs qui se chevauchent.
 *
 * Ne s'exécute que si SPRING_DATASOURCE_URL pointe vers PostgreSQL (CI GitHub Actions,
 * ou en local avec une base PostgreSQL de test). Chaque test est annulé (rollback) à la fin.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "SPRING_DATASOURCE_URL", matches = "jdbc:postgresql:.*")
class PostgreSqlIntegrationTest {

    private static final LocalDateTime DIX_HEURES = LocalDateTime.of(2030, 6, 3, 10, 0);

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private RendezVousRepository rendezVousRepository;
    @Autowired private MedecinRepository medecinRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private TypeRendezVousRepository typeRendezVousRepository;

    private Medecin medecin;
    private Patient patient;
    private String nomPatient;
    private TypeRendezVous consultation;

    @BeforeEach
    void setUp() {
        String suffixe = UUID.randomUUID().toString();
        medecin = new Medecin();
        medecin.setEmail("medecin-" + suffixe + "@ci.local");
        medecin.setPassword("hash");
        medecin.setRole(Roles.MEDECIN);
        medecin = medecinRepository.save(medecin);

        patient = new Patient();
        patient.setEmail("patient-" + suffixe + "@ci.local");
        nomPatient = "Ngono" + suffixe.substring(0, 8);
        patient.setNom(nomPatient);
        patient.setPrenom("Aïcha");
        patient = patientRepository.save(patient);

        consultation = typeRendezVousRepository.save(
                new TypeRendezVous(null, "Consultation CI " + suffixe, 30, new BigDecimal("15000.00")));
    }

    private RendezVous rendezVous(Medecin medecinDuRdv, LocalDateTime debut, StatutRendezVous statut) {
        RendezVous rendezVous = new RendezVous();
        rendezVous.setMedecin(medecinDuRdv);
        rendezVous.setPatient(patient);
        rendezVous.setTypeRendezVous(consultation);
        rendezVous.setDateRendezVous(debut);
        rendezVous.setDateTimeFinRendezVousPossible(debut.plusMinutes(30));
        rendezVous.setStatutRendezVous(statut);
        return rendezVous;
    }

    @Test
    void flywayAppliedBothMigrations() {
        List<String> versions = jdbcTemplate.queryForList(
                "SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank", String.class);
        assertThat(versions).contains("1", "2", "3", "4");
    }

    @Test
    void databaseRejectsOverlappingActiveAppointments() {
        rendezVousRepository.saveAndFlush(rendezVous(medecin, DIX_HEURES, StatutRendezVous.CONFIRME));

        assertThatThrownBy(() -> rendezVousRepository.saveAndFlush(
                rendezVous(medecin, DIX_HEURES.plusMinutes(15), StatutRendezVous.EN_ATTENTE)))
                .isInstanceOfSatisfying(DataIntegrityViolationException.class, e ->
                        // Même détection que GlobalExceptionHandler (réponse 409)
                        assertThat(e.getMostSpecificCause().getMessage()).contains("ex_rendez_vous_medecin_chevauchement"));
    }

    @Test
    void databaseAcceptsBackToBackAppointments() {
        rendezVousRepository.saveAndFlush(rendezVous(medecin, DIX_HEURES, StatutRendezVous.CONFIRME));
        rendezVousRepository.saveAndFlush(rendezVous(medecin, DIX_HEURES.plusMinutes(30), StatutRendezVous.CONFIRME));
    }

    @Test
    void cancelledAppointmentDoesNotBlockTheSlot() {
        rendezVousRepository.saveAndFlush(rendezVous(medecin, DIX_HEURES, StatutRendezVous.ANNULER));
        rendezVousRepository.saveAndFlush(rendezVous(medecin, DIX_HEURES, StatutRendezVous.CONFIRME));
    }

    @Test
    void appointmentSearchRunsOnPostgreSql() {
        RendezVous confirme = rendezVousRepository.saveAndFlush(rendezVous(medecin, DIX_HEURES, StatutRendezVous.CONFIRME));
        rendezVousRepository.saveAndFlush(rendezVous(medecin, DIX_HEURES.plusDays(1), StatutRendezVous.EN_ATTENTE));

        // Texte (casse ignorée, prénom puis nom), statut et jour : même SQL que la page Rendez-vous de l'admin
        assertThat(rendezVousRepository.findAll(RendezVousSpecifications.rechercher(
                        "aïcha " + nomPatient.toUpperCase(), StatutRendezVous.CONFIRME, DIX_HEURES.toLocalDate()),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "dateRendezVous"))).getContent())
                .containsExactly(confirme);
        assertThat(rendezVousRepository.findAll(RendezVousSpecifications.rechercher(nomPatient, null, null),
                PageRequest.of(0, 1)).getTotalElements()).isEqualTo(2);
        assertThat(rendezVousRepository.compterParStatut()).isNotEmpty();
    }
}
