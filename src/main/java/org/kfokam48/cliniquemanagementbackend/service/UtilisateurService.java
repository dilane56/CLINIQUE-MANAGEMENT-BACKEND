package org.kfokam48.cliniquemanagementbackend.service;

import org.kfokam48.cliniquemanagementbackend.dto.UtilisateurDTO;
import org.kfokam48.cliniquemanagementbackend.dto.UtilisateurResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UtilisateurService {
    Utilisateur findById(Long id);
    Utilisateur update(Long id, UtilisateurDTO utilisateurDTO);
    Utilisateur findByEmail(String email);
    ResponseEntity<String> deleteById(Long id);
    List<UtilisateurResponseDTO> findAll();
    boolean existsByEmail(String email);
    Utilisateur addRoleTouser(Utilisateur utilisateur, String role);
}
