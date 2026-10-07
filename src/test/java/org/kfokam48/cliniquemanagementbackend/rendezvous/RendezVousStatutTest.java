package org.kfokam48.cliniquemanagementbackend.rendezvous;

import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.mapper.RendezVousMapper;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.*;
import org.kfokam48.cliniquemanagementbackend.service.impl.RendezVousServiceImpl;
import org.kfokam48.cliniquemanagementbackend.service.mail.EmailService;
import org.kfokam48.cliniquemanagementbackend.service.notification.NotificationService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.kfokam48.cliniquemanagementbackend.rendezvous.RendezVousTestData.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * I18 : changement de statut d'un rendez-vous.
 */
class RendezVousStatutTest {

    private final RendezVousRepository rendezVousRepository = mock(RendezVousRepository.class);
    private final RendezVousServiceImpl service = new RendezVousServiceImpl(rendezVousRepository,
            mock(RendezVousMapper.class), mock(PatientRepository.class), mock(MedecinRepository.class),
            mock(TypeRendezVousRepository.class), mock(SecretaireRepository.class),
            mock(NotificationService.class), mock(EmailService.class));

    private RendezVous enregistre(LocalDateTime debut, StatutRendezVous statut) {
        Medecin medecin = medecin("medecin@test.com");
        medecin.setId(7L);
        Patient patient = patient("patient@test.com");
        RendezVous rendezVous = rendezVous(medecin, patient, consultation30Minutes(), debut, statut);
        rendezVous.setId(50L);
        when(rendezVousRepository.findById(50L)).thenReturn(Optional.of(rendezVous));
        when(rendezVousRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        return rendezVous;
    }

    @Test
    void cannotStartAnAppointmentBeforeItsTime() {
        RendezVous rendezVous = enregistre(LocalDateTime.now().plusHours(2), StatutRendezVous.CONFIRME);

        assertThatThrownBy(() -> service.updateStatut(50L, StatutRendezVous.EN_COURS))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rendezVous.getStatutRendezVous()).isEqualTo(StatutRendezVous.CONFIRME);
        verify(rendezVousRepository, never()).save(any());
    }

    @Test
    void canStartAnAppointmentOnceItsTimeHasCome() {
        RendezVous rendezVous = enregistre(LocalDateTime.now().minusMinutes(5), StatutRendezVous.CONFIRME);
        service.updateStatut(50L, StatutRendezVous.EN_COURS);
        assertThat(rendezVous.getStatutRendezVous()).isEqualTo(StatutRendezVous.EN_COURS);
    }

    @Test
    void canConfirmOrCancelAFutureAppointment() {
        RendezVous rendezVous = enregistre(LocalDateTime.now().plusDays(1), StatutRendezVous.EN_ATTENTE);
        service.updateStatut(50L, StatutRendezVous.CONFIRME);
        assertThat(rendezVous.getStatutRendezVous()).isEqualTo(StatutRendezVous.CONFIRME);
        service.updateStatut(50L, StatutRendezVous.ANNULER);
        assertThat(rendezVous.getStatutRendezVous()).isEqualTo(StatutRendezVous.ANNULER);
    }
}
