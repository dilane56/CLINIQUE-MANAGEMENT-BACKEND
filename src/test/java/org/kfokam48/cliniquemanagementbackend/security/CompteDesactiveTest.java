package org.kfokam48.cliniquemanagementbackend.security;

import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.dto.auth.LoginRequest;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;
import org.kfokam48.cliniquemanagementbackend.enums.UserStatus;
import org.kfokam48.cliniquemanagementbackend.exception.AuthenticationFailedException;
import org.kfokam48.cliniquemanagementbackend.exception.CompteDesactiveException;
import org.kfokam48.cliniquemanagementbackend.mapper.UtilisateurMapper;
import org.kfokam48.cliniquemanagementbackend.model.Administrateur;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.cliniquemanagementbackend.service.auth.AuthService;
import org.kfokam48.cliniquemanagementbackend.service.auth.CustomUserDetailsService;
import org.kfokam48.cliniquemanagementbackend.service.auth.LoginAttemptService;
import org.kfokam48.cliniquemanagementbackend.service.impl.UtilisateurServiceImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * I5 : un administrateur peut désactiver un compte du personnel. Le compte ne peut plus se
 * connecter ; ses jetons déjà émis sont refusés (voir AccessControlTest et
 * WebSocketAuthInterceptorTest).
 */
class CompteDesactiveTest {

    private static final String EMAIL = "medecin@clinique.com";
    private static final String MOT_DE_PASSE = "MotDePasse123";

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final UtilisateurRepository repository = mock(UtilisateurRepository.class);

    private Medecin medecin(boolean actif) {
        Medecin medecin = new Medecin();
        medecin.setId(7L);
        medecin.setEmail(EMAIL);
        medecin.setPassword(encoder.encode(MOT_DE_PASSE));
        medecin.setRole(Roles.MEDECIN);
        medecin.setStatus(UserStatus.EN_LIGNE);
        medecin.setActif(actif);
        return medecin;
    }

    // --- Spring Security voit l'état du compte ---

    @Test
    void userDetailsReflectTheAccountState() {
        CustomUserDetailsService service = new CustomUserDetailsService(repository);
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(medecin(true)));
        assertThat(service.loadUserByUsername(EMAIL).isEnabled()).isTrue();

        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(medecin(false)));
        assertThat(service.loadUserByUsername(EMAIL).isEnabled()).isFalse();
    }

    // --- Connexion ---

    private LoginAttemptService loginAttemptService;

    private AuthService authServiceSurCompteDesactive() {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(medecin(false)));
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        // Comportement de Spring Security : compte désactivé signalé avant le contrôle du mot de passe
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("User is disabled"));
        loginAttemptService = mock(LoginAttemptService.class);
        return new AuthService(authenticationManager, mock(UserDetailsService.class), repository,
                mock(UtilisateurMapper.class), loginAttemptService, encoder);
    }

    private static LoginRequest connexion(String motDePasse) {
        LoginRequest requete = new LoginRequest();
        requete.setEmail(EMAIL);
        requete.setPassword(motDePasse);
        return requete;
    }

    @Test
    void disabledAccountWithCorrectPasswordGetsAClearMessage() {
        AuthService authService = authServiceSurCompteDesactive();
        assertThatThrownBy(() -> authService.authenticateUser(connexion(MOT_DE_PASSE)))
                .isInstanceOf(CompteDesactiveException.class)
                .hasMessage("Ce compte est désactivé. Contactez l'administrateur.");
        verify(loginAttemptService, never()).enregistrerEchec(any());
    }

    @Test
    void disabledAccountWithWrongPasswordRevealsNothing() {
        AuthService authService = authServiceSurCompteDesactive();
        // Même réponse que pour un compte inexistant : on ne révèle pas l'état du compte
        assertThatThrownBy(() -> authService.authenticateUser(connexion("mauvais")))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Identifiants invalides : vérifiez l'e-mail ou le mot de passe.");
        verify(loginAttemptService).enregistrerEchec(EMAIL);
    }

    // --- Désactivation par l'administrateur ---

    @Test
    void adminDisablesAndReenablesAnAccount() {
        Medecin medecin = medecin(true);
        when(repository.findById(7L)).thenReturn(Optional.of(medecin));
        when(repository.save(medecin)).thenReturn(medecin);
        UtilisateurServiceImpl service = new UtilisateurServiceImpl(repository, mock(UtilisateurMapper.class));

        service.changerActivation(7L, false, "admin@clinique.com");
        assertThat(medecin.isActif()).isFalse();
        // Il n'apparaît plus « en ligne » dans le chat
        assertThat(medecin.getStatus()).isEqualTo(UserStatus.HORS_LIGNE);

        service.changerActivation(7L, true, "admin@clinique.com");
        assertThat(medecin.isActif()).isTrue();
    }

    @Test
    void adminCannotDisableHisOwnAccount() {
        Administrateur admin = new Administrateur();
        admin.setId(1L);
        admin.setEmail("admin@clinique.com");
        when(repository.findById(1L)).thenReturn(Optional.of(admin));
        UtilisateurServiceImpl service = new UtilisateurServiceImpl(repository, mock(UtilisateurMapper.class));

        assertThatThrownBy(() -> service.changerActivation(1L, false, "ADMIN@clinique.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Vous ne pouvez pas désactiver votre propre compte.");
        assertThat(admin.isActif()).isTrue();
        verify(repository, never()).save(any());
    }
}
