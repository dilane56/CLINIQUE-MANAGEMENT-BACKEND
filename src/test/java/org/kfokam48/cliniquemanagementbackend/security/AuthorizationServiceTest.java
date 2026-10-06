package org.kfokam48.cliniquemanagementbackend.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.repository.*;
import org.kfokam48.cliniquemanagementbackend.service.auth.AuthorizationService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthorizationServiceTest {

    private static final String EMAIL = "medecin@clinique.com";
    private static final long MEDECIN_ID = 1L;

    private final UtilisateurRepository utilisateurRepository = mock(UtilisateurRepository.class);
    private final RendezVousRepository rendezVousRepository = mock(RendezVousRepository.class);
    private final FactureRepository factureRepository = mock(FactureRepository.class);
    private final PrescriptionRepository prescriptionRepository = mock(PrescriptionRepository.class);
    private final LignePrescriptionRepository lignePrescriptionRepository = mock(LignePrescriptionRepository.class);
    private final LigneFactureRepository ligneFactureRepository = mock(LigneFactureRepository.class);

    private final AuthorizationService authz = new AuthorizationService(
            utilisateurRepository, mock(NotificationRepository.class), rendezVousRepository,
            factureRepository, prescriptionRepository, lignePrescriptionRepository, ligneFactureRepository);

    @BeforeEach
    void connecterMedecin() {
        Medecin medecin = new Medecin();
        medecin.setId(MEDECIN_ID);
        medecin.setEmail(EMAIL);
        when(utilisateurRepository.findByEmail(EMAIL)).thenReturn(Optional.of(medecin));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                EMAIL, null, List.of(new SimpleGrantedAuthority("ROLE_MEDECIN"))));
    }

    @AfterEach
    void nettoyer() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ownsRendezVousChecksTheConnectedMedecin() {
        when(rendezVousRepository.existsByIdAndMedecin_Id(10L, MEDECIN_ID)).thenReturn(true);
        assertThat(authz.ownsRendezVous(10L)).isTrue();
        assertThat(authz.ownsRendezVous(11L)).isFalse();
    }

    @Test
    void ownsFacturePrescriptionAndLinesUseTheConnectedMedecin() {
        when(factureRepository.existsByIdAndRendezVous_Medecin_Id(7L, MEDECIN_ID)).thenReturn(true);
        when(prescriptionRepository.existsByIdAndRendezVous_Medecin_Id(4L, MEDECIN_ID)).thenReturn(true);
        when(lignePrescriptionRepository.existsByIdAndPrescription_RendezVous_Medecin_Id(2L, MEDECIN_ID)).thenReturn(true);
        when(ligneFactureRepository.existsByIdAndFacture_RendezVous_Medecin_Id(3L, MEDECIN_ID)).thenReturn(true);

        assertThat(authz.ownsFacture(7L)).isTrue();
        assertThat(authz.ownsPrescription(4L)).isTrue();
        assertThat(authz.ownsLignePrescription(2L)).isTrue();
        assertThat(authz.ownsLigneFacture(3L)).isTrue();
        assertThat(authz.ownsFacture(8L)).isFalse();
    }

    @Test
    void nullIdIsNeverOwned() {
        assertThat(authz.ownsRendezVous(null)).isFalse();
        assertThat(authz.isCurrentUser(null)).isFalse();
    }

    @Test
    void anonymousOwnsNothing() {
        SecurityContextHolder.clearContext();
        when(rendezVousRepository.existsByIdAndMedecin_Id(10L, MEDECIN_ID)).thenReturn(true);
        assertThat(authz.ownsRendezVous(10L)).isFalse();
        assertThat(authz.isCurrentUser(MEDECIN_ID)).isFalse();
    }
}
