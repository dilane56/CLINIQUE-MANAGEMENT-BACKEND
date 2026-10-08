package org.kfokam48.cliniquemanagementbackend.security;

import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.dto.AdministrateurDTO;
import org.kfokam48.cliniquemanagementbackend.dto.medecin.MedecinDTO;
import org.kfokam48.cliniquemanagementbackend.dto.secretaire.SecretaireDTO;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;
import org.kfokam48.cliniquemanagementbackend.mapper.AdministrateurMapper;
import org.kfokam48.cliniquemanagementbackend.mapper.MedecinMapper;
import org.kfokam48.cliniquemanagementbackend.mapper.SecretaireMapper;
import org.kfokam48.cliniquemanagementbackend.model.Administrateur;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.model.Secretaire;
import org.kfokam48.cliniquemanagementbackend.repository.AdministrateurRepository;
import org.kfokam48.cliniquemanagementbackend.repository.MedecinRepository;
import org.kfokam48.cliniquemanagementbackend.repository.SecretaireRepository;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.cliniquemanagementbackend.service.impl.AdministrateurServiceImpl;
import org.kfokam48.cliniquemanagementbackend.service.impl.MedecinServiceImpl;
import org.kfokam48.cliniquemanagementbackend.service.impl.SecretaireServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Un médecin ou une secrétaire a le droit de modifier son propre profil. Avant ce correctif, la
 * mise à jour recopiait le champ "role" de la requête : il suffisait d'envoyer "role": "ADMIN"
 * pour devenir administrateur. Le rôle est désormais fixé par le type de compte.
 */
class EscaladeDeRoleTest {

    private final UtilisateurRepository utilisateurRepository = mock(UtilisateurRepository.class);

    @Test
    void medecinCannotPromoteHimselfToAdmin() {
        Medecin medecin = new Medecin();
        medecin.setId(7L);
        medecin.setEmail("medecin@clinique.com");
        medecin.setRole(Roles.MEDECIN);
        MedecinRepository repository = mock(MedecinRepository.class);
        when(repository.findById(7L)).thenReturn(Optional.of(medecin));
        MedecinServiceImpl service = new MedecinServiceImpl(repository, mock(MedecinMapper.class), utilisateurRepository);

        MedecinDTO dto = new MedecinDTO();
        dto.setEmail("medecin@clinique.com");
        dto.setPassword("NouveauMotDePasse1");
        dto.setNom("Martin");
        dto.setRole(Roles.ADMIN);
        service.update(7L, dto);

        assertThat(medecin.getRole()).isEqualTo(Roles.MEDECIN);
        assertThat(medecin.getNom()).isEqualTo("Martin");
    }

    @Test
    void secretaireCannotPromoteHerselfToAdmin() {
        Secretaire secretaire = new Secretaire();
        secretaire.setId(4L);
        secretaire.setEmail("secretaire@clinique.com");
        secretaire.setRole(Roles.SECRETAIRE);
        SecretaireRepository repository = mock(SecretaireRepository.class);
        when(repository.findById(4L)).thenReturn(Optional.of(secretaire));
        SecretaireServiceImpl service = new SecretaireServiceImpl(repository, mock(SecretaireMapper.class), utilisateurRepository);

        SecretaireDTO dto = new SecretaireDTO();
        dto.setEmail("secretaire@clinique.com");
        dto.setPassword("NouveauMotDePasse1");
        dto.setRole(Roles.ADMIN);
        service.update(4L, dto);

        assertThat(secretaire.getRole()).isEqualTo(Roles.SECRETAIRE);
    }

    @Test
    void adminCanBeUpdatedWithHisOwnEmailAndKeepsHisRole() {
        Administrateur admin = new Administrateur();
        admin.setId(1L);
        admin.setEmail("admin@clinique.com");
        admin.setRole(Roles.ADMIN);
        AdministrateurRepository repository = mock(AdministrateurRepository.class);
        when(repository.findById(1L)).thenReturn(Optional.of(admin));
        // Son propre e-mail existe forcément en base : il ne doit pas être refusé
        when(utilisateurRepository.existsByEmail("admin@clinique.com")).thenReturn(true);
        AdministrateurServiceImpl service = new AdministrateurServiceImpl(repository, mock(AdministrateurMapper.class), utilisateurRepository);

        AdministrateurDTO dto = new AdministrateurDTO();
        dto.setEmail("admin@clinique.com");
        dto.setPassword("NouveauMotDePasse1");
        dto.setNom("Principal");
        dto.setRole(Roles.MEDECIN);
        service.update(1L, dto);

        assertThat(admin.getNom()).isEqualTo("Principal");
        assertThat(admin.getRole()).isEqualTo(Roles.ADMIN);
    }
}
