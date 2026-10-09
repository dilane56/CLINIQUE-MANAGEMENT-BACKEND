package org.kfokam48.cliniquemanagementbackend.service.impl;

import org.kfokam48.cliniquemanagementbackend.service.Compteurs;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;
import java.util.Map;
import org.kfokam48.cliniquemanagementbackend.dto.auth.UserDTO;
import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.ProfilUpdateDTO;
import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.Contact;
import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.UtilisateurDTO;
import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.UtilisateurResponseDTO;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.mapper.UtilisateurMapper;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.cliniquemanagementbackend.service.UtilisateurService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.kfokam48.cliniquemanagementbackend.enums.UserStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional
public class UtilisateurServiceImpl implements UtilisateurService {
    private final UtilisateurRepository utilisateurRepository;
     private final UtilisateurMapper utilisateurMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UtilisateurServiceImpl(UtilisateurRepository utilisateurRepository, UtilisateurMapper utilisateurMapper) {
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurMapper = utilisateurMapper;
    }



    @Override
    public Utilisateur findById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable : " + id));
    }

    @Override
    public Utilisateur update(Long id,@Valid UtilisateurDTO utilisateurDTO) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable : " + id));
        if (!utilisateurRepository.existsByEmail(utilisateurDTO.getEmail())) {

            utilisateur.setEmail(utilisateurDTO.getEmail());
            utilisateur.setPassword(passwordEncoder.encode(utilisateurDTO.getPassword()));
            utilisateurRepository.save(utilisateur);
        }
        return utilisateur;
    }

    @Override
    public Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable : " + email));
    }



    @Override
    public void deleteById(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable : " + id));
        utilisateurRepository.deleteById(id);
    }

    @Override
    public List<UtilisateurResponseDTO> findAll() {
        return utilisateurMapper.utilisateursToUtilisateurResponseDTOs(utilisateurRepository.findAll());
    }

    @Override
    public boolean existsByEmail(String email) {
        return utilisateurRepository.existsByEmail(email);
    }

    @Override
    public Utilisateur addRoleTouser(Utilisateur utilisateur, String role) {
        return null;
    }

    @Override
    public List<Contact> findAllContacts() {
        return utilisateurMapper.utilisateursToContacts(utilisateurRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UtilisateurResponseDTO> findAll(Pageable pageable) {
        return PageResponse.of(utilisateurRepository.findAll(pageable), utilisateurMapper::utilisateursToUtilisateurResponseDTOs);
    }

    @Override
    public UserDTO updateProfil(String email, ProfilUpdateDTO profil) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable"));
        utilisateur.setNom(profil.getNom().trim());
        utilisateur.setPrenom(profil.getPrenom().trim());
        utilisateur.setTelephone(profil.getTelephone());
        utilisateur.setAdresse(profil.getAdresse());
        return utilisateurMapper.utilisateurToUserDTO(utilisateurRepository.save(utilisateur));
    }

    @Override
    public UtilisateurResponseDTO changerActivation(Long id, boolean actif, String emailAdministrateur) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable"));
        // Garde-fou : l'administrateur ne peut pas se bloquer lui-même hors de l'application
        if (!actif && utilisateur.getEmail().equalsIgnoreCase(emailAdministrateur)) {
            throw new IllegalStateException("Vous ne pouvez pas désactiver votre propre compte.");
        }
        utilisateur.setActif(actif);
        if (!actif) {
            // Il n'apparaît plus « en ligne » dans le chat
            utilisateur.setStatus(UserStatus.HORS_LIGNE);
        }
        return utilisateurMapper.utilisateurToUtilisateurResponseDTO(utilisateurRepository.save(utilisateur));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Roles, Long> compterParRole() {
        return Compteurs.parValeur(Roles.class, utilisateurRepository.compterParRole());
    }
}
