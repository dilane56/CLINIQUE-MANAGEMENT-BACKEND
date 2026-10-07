package org.kfokam48.cliniquemanagementbackend.service;

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
}
