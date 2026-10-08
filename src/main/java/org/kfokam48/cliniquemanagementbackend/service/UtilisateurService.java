package org.kfokam48.cliniquemanagementbackend.service;

import org.kfokam48.cliniquemanagementbackend.enums.Roles;
import java.util.Map;
import org.kfokam48.cliniquemanagementbackend.dto.auth.UserDTO;
import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.ProfilUpdateDTO;
import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.Contact;
import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.UtilisateurDTO;
import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.UtilisateurResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;

import java.util.List;

public interface UtilisateurService {
    Utilisateur findById(Long id);
    Utilisateur update(Long id, UtilisateurDTO utilisateurDTO);
    Utilisateur findByEmail(String email);
    void deleteById(Long id);
    List<UtilisateurResponseDTO> findAll();
    boolean existsByEmail(String email);
    Utilisateur addRoleTouser(Utilisateur utilisateur, String role);
    List<Contact> findAllContacts();

    PageResponse<UtilisateurResponseDTO> findAll(Pageable pageable);

    // Modification de son propre profil (nom, prénom, téléphone, adresse)
    UserDTO updateProfil(String email, ProfilUpdateDTO profil);

    // I5 : désactive ou réactive un compte (admin) ; l'administrateur ne peut pas se désactiver lui-même
    UtilisateurResponseDTO changerActivation(Long id, boolean actif, String emailAdministrateur);

    // Nombre de comptes par rôle (tous les rôles, 0 si aucun)
    Map<Roles, Long> compterParRole();
}
