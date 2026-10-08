package org.kfokam48.cliniquemanagementbackend.rendezvous;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.service.notification.NotificationService;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.mapper.RendezVousMapperImpl;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.kfokam48.cliniquemanagementbackend.model.TypeRendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.MedecinRepository;
import org.kfokam48.cliniquemanagementbackend.repository.PatientRepository;
import org.kfokam48.cliniquemanagementbackend.repository.RendezVousRepository;
import org.kfokam48.cliniquemanagementbackend.repository.TypeRendezVousRepository;
import org.kfokam48.cliniquemanagementbackend.service.impl.RendezVousServiceImpl;
import org.kfokam48.cliniquemanagementbackend.service.mail.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kfokam48.cliniquemanagementbackend.rendezvous.RendezVousTestData.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;

/**
 * I10 : deux réservations simultanées du même créneau ne doivent pas réussir toutes les deux.
 *
 * La réservation A est bloquée volontairement juste avant son commit (pendant l'envoi de la
 * notification, appelé dans la transaction) ; la réservation B est lancée pendant ce temps.
 * Grâce au verrou SELECT ... FOR UPDATE sur le médecin, B attend la fin de A puis voit le
 * rendez-vous de A et est refusée. Sans le verrou, B ne voit rien et les deux réussissent.
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:concurrence;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        // Indépendant du profil actif (la CI utilise "prod", qui impose le dialecte PostgreSQL)
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({RendezVousServiceImpl.class, RendezVousMapperImpl.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED) // chaque réservation committe réellement
class RendezVousConcurrenceTest {

    @Autowired private RendezVousServiceImpl rendezVousService;
    @Autowired private RendezVousRepository rendezVousRepository;
    @Autowired private MedecinRepository medecinRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private TypeRendezVousRepository typeRendezVousRepository;

    @MockitoBean private NotificationService notificationService;
    @MockitoBean private EmailService emailService;

    @AfterEach
    void nettoyer() {
        rendezVousRepository.deleteAll();
        patientRepository.deleteAll();
        medecinRepository.deleteAll();
        typeRendezVousRepository.deleteAll();
    }

    private static RendezVousDTO demande(Long medecinId, Long patientId, Long typeId, LocalDateTime debut) {
        RendezVousDTO dto = new RendezVousDTO();
        dto.setMedecinId(medecinId);
        dto.setPatientId(patientId);
        dto.setTypeRendezVousId(typeId);
        dto.setDateRendezVous(debut);
        dto.setMotif("Consultation");
        return dto;
    }

    @Test
    void twoSimultaneousBookingsOfTheSameSlotCannotBothSucceed() throws Exception {
        Medecin medecin = medecinRepository.save(medecin("medecin@test.com"));
        Patient patientA = patientRepository.save(patient("a@test.com"));
        Patient patientB = patientRepository.save(patient("b@test.com"));
        TypeRendezVous type = typeRendezVousRepository.save(consultation30Minutes());
        LocalDateTime creneau = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);

        // A s'arrête dans sa transaction (verrou tenu) tant que B n'a pas eu le temps de démarrer
        CountDownLatch aTientLeVerrou = new CountDownLatch(1);
        AtomicBoolean premiereNotification = new AtomicBoolean(true);
        doAnswer(invocation -> {
            if (premiereNotification.getAndSet(false)) {
                aTientLeVerrou.countDown();
                Thread.sleep(1500);
            }
            return null;
        }).when(notificationService).sendNotification(anyLong(), anyString(), anyString(), anyBoolean());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> reservationA = executor.submit(() ->
                    rendezVousService.save(demande(medecin.getId(), patientA.getId(), type.getId(), creneau)));
            assertThat(aTientLeVerrou.await(10, TimeUnit.SECONDS)).isTrue();
            Future<?> reservationB = executor.submit(() ->
                    rendezVousService.save(demande(medecin.getId(), patientB.getId(), type.getId(), creneau.plusMinutes(15))));

            reservationA.get(20, TimeUnit.SECONDS);
            assertThat(catchCause(reservationB))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("créneau est déjà pris");
        } finally {
            executor.shutdownNow();
        }

        assertThat(rendezVousRepository.findByMedecinId(medecin.getId())).hasSize(1);
    }

    private static Throwable catchCause(Future<?> future) throws Exception {
        try {
            future.get(20, TimeUnit.SECONDS);
            return null;
        } catch (ExecutionException e) {
            return e.getCause();
        }
    }
}
