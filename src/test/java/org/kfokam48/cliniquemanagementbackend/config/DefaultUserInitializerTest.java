package org.kfokam48.cliniquemanagementbackend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;
import org.kfokam48.cliniquemanagementbackend.model.Administrateur;
import org.kfokam48.cliniquemanagementbackend.repository.AdministrateurRepository;
import org.kfokam48.cliniquemanagementbackend.repository.TypeRendezVousRepository;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultUserInitializerTest {

    private AdministrateurRepository administrateurRepository;
    private UtilisateurRepository utilisateurRepository;
    private PasswordEncoder passwordEncoder;
    private DefaultUserInitializer initializer;

    @BeforeEach
    void setUp() {
        administrateurRepository = mock(AdministrateurRepository.class);
        utilisateurRepository = mock(UtilisateurRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        TypeRendezVousRepository typeRendezVousRepository = mock(TypeRendezVousRepository.class);
        when(typeRendezVousRepository.existsByLibelle(any())).thenReturn(true);
        initializer = new DefaultUserInitializer(administrateurRepository, utilisateurRepository, passwordEncoder, typeRendezVousRepository);
    }

    private void configure(String email, String password) {
        ReflectionTestUtils.setField(initializer, "defaultAdminEmail", email);
        ReflectionTestUtils.setField(initializer, "defaultAdminPassword", password);
    }

    @Test
    void noAdminCreatedWhenVariablesMissing() throws Exception {
        configure("", "");
        initializer.run();
        verify(administrateurRepository, never()).save(any());
    }

    @Test
    void noAdminCreatedWhenPasswordTooShort() throws Exception {
        configure("admin@clinique.com", "court");
        initializer.run();
        verify(administrateurRepository, never()).save(any());
    }

    @Test
    void noAdminCreatedWhenEmailAlreadyUsed() throws Exception {
        configure("admin@clinique.com", "MotDePasseFort1");
        when(utilisateurRepository.existsByEmail("admin@clinique.com")).thenReturn(true);
        initializer.run();
        verify(administrateurRepository, never()).save(any());
    }

    @Test
    void adminCreatedFromEnvironmentWithEncodedPassword() throws Exception {
        configure("admin@clinique.com", "MotDePasseFort1");
        when(passwordEncoder.encode("MotDePasseFort1")).thenReturn("hash");

        initializer.run();

        ArgumentCaptor<Administrateur> captor = ArgumentCaptor.forClass(Administrateur.class);
        verify(administrateurRepository).save(captor.capture());
        Administrateur admin = captor.getValue();
        assertThat(admin.getEmail()).isEqualTo("admin@clinique.com");
        assertThat(admin.getPassword()).isEqualTo("hash");
        assertThat(admin.getRole()).isEqualTo(Roles.ADMIN);
    }
}
