package org.kfokam48.cliniquemanagementbackend.rendezvous;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.service.notification.NotificationService;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.mapper.RendezVousMapper;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.kfokam48.cliniquemanagementbackend.model.TypeRendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.*;
import org.kfokam48.cliniquemanagementbackend.service.impl.RendezVousServiceImpl;
import org.kfokam48.cliniquemanagementbackend.service.mail.EmailService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.kfokam48.cliniquemanagementbackend.rendezvous.RendezVousTestData.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * I20 : les patients ne sont pas des utilisateurs. Ils doivent recevoir un e-mail à leur propre
 * adresse, et aucune notification ne doit partir avec leur identifiant (qui désignerait le membre
 * du personnel ayant le même numéro).
 */
class RendezVousNotificationTest {

    private static final long PATIENT_ID = 3L;   // même numéro qu'un membre du personnel
    private static final long MEDECIN_ID = 7L;
    private static final long TYPE_ID = 1L;

    private final RendezVousRepository rendezVousRepository = mock(RendezVousRepository.class);
    private final MedecinRepository medecinRepository = mock(MedecinRepository.class);
    private final PatientRepository patientRepository = mock(PatientRepository.class);
    private final TypeRendezVousRepository typeRendezVousRepository = mock(TypeRendezVousRepository.class);
    private final SecretaireRepository secretaireRepository = mock(SecretaireRepository.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final EmailService emailService = mock(EmailService.class);

    private final RendezVousServiceImpl service = new RendezVousServiceImpl(rendezVousRepository,
            mock(RendezVousMapper.class), patientRepository, medecinRepository, typeRendezVousRepository,
            secretaireRepository, notificationService, emailService);

    private Medecin medecin;
    private Patient patient;
    private TypeRendezVous type;

    @BeforeEach
    void setUp() {
        medecin = medecin("medecin@test.com");
        medecin.setId(MEDECIN_ID);
        medecin.setNom("Martin");
        patient = patient("patient@test.com");
        patient.setId(PATIENT_ID);
        type = consultation30Minutes();
        type.setId(TYPE_ID);

        when(medecinRepository.findByIdForUpdate(MEDECIN_ID)).thenReturn(Optional.of(medecin));
        when(patientRepository.findByIdForUpdate(PATIENT_ID)).thenReturn(Optional.of(patient));
        when(typeRendezVousRepository.findById(TYPE_ID)).thenReturn(Optional.of(type));
        when(rendezVousRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private RendezVousDTO demande(Long secretaireId) {
        RendezVousDTO dto = new RendezVousDTO();
        dto.setMedecinId(MEDECIN_ID);
        dto.setPatientId(PATIENT_ID);
        dto.setTypeRendezVousId(TYPE_ID);
        dto.setSecretaireId(secretaireId);
        dto.setDateRendezVous(LocalDateTime.now().plusDays(2).withHour(10).withMinute(0));
        return dto;
    }

    @Test
    void newAppointmentEmailsThePatientAndNotifiesOnlyTheMedecin() {
        service.save(demande(null));

        verify(emailService).sendEmail(eq("patient@test.com"), anyString(), contains("Dr Martin"));
        verify(notificationService).sendNotification(eq(MEDECIN_ID), anyString(), anyString(), anyBoolean());
        verify(notificationService, never()).sendNotification(eq(PATIENT_ID), anyString(), anyString(), anyBoolean());
    }

    @Test
    void statusChangeEmailsThePatientInsteadOfNotifyingStaffWithTheSameId() {
        RendezVous rendezVous = rendezVous(medecin, patient, type, LocalDateTime.now().plusDays(2), StatutRendezVous.EN_ATTENTE);
        rendezVous.setId(50L);
        when(rendezVousRepository.findById(50L)).thenReturn(Optional.of(rendezVous));

        service.updateStatut(50L, StatutRendezVous.CONFIRME);

        verify(emailService).sendEmail(eq("patient@test.com"), anyString(), contains("CONFIRME"));
        verify(notificationService, never()).sendNotification(eq(PATIENT_ID), anyString(), anyString(), anyBoolean());
    }

    @Test
    void unknownSecretaireIdIsRejected() {
        when(secretaireRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.save(demande(99L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Secrétaire introuvable");
        verify(rendezVousRepository, never()).save(any());
    }

    @Test
    void existingSecretaireIdIsAccepted() {
        when(secretaireRepository.existsById(4L)).thenReturn(true);
        service.save(demande(4L));
        verify(rendezVousRepository).save(any());
    }
}
