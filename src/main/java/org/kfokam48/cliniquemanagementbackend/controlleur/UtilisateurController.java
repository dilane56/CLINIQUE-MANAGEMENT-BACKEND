package org.kfokam48.cliniquemanagementbackend.controlleur;
import org.springframework.security.core.Authentication;
import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.ProfilUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.dto.auth.UserDTO;
import jakarta.validation.Valid;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import org.springframework.security.access.prepost.PreAuthorize;


import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.Contact;
import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.UtilisateurResponseDTO;
import org.kfokam48.cliniquemanagementbackend.mapper.UtilisateurMapper;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;

import org.kfokam48.cliniquemanagementbackend.service.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/utilisateurs") // URL de base pour le contrôleur
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final UtilisateurMapper utilisateurMapper;

    public UtilisateurController(UtilisateurService utilisateurService, UtilisateurMapper utilisateurMapper) {
        this.utilisateurService = utilisateurService;
        this.utilisateurMapper = utilisateurMapper;
    }



    // Modification de son propre profil : l'utilisateur est celui du JWT, jamais un identifiant fourni par le client
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> updateMonProfil(@Valid @RequestBody ProfilUpdateDTO profil, Authentication authentication) {
        return ResponseEntity.ok(utilisateurService.updateProfil(authentication.getName(), profil));
    }

    // Endpoint pour récupérer un utilisateur par ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE','MEDECIN')")
    public ResponseEntity<UtilisateurResponseDTO> getUtilisateurById(@PathVariable Long id) {
        Utilisateur utilisateur = utilisateurService.findById(id);
        return new ResponseEntity<>(utilisateurMapper.utilisateurToUtilisateurResponseDTO(utilisateur), HttpStatus.OK);
    }


    // Endpoint pour supprimer un utilisateur par ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUtilisateur(@PathVariable Long id) {
        utilisateurService.deleteById(id);
        return ResponseEntity.ok("Utilisateur deleted successfully");
    }

    // Endpoint pour récupérer tous les utilisateurs
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE','MEDECIN')")
    public ResponseEntity<List<UtilisateurResponseDTO>> getAllUtilisateurs() {
        List<UtilisateurResponseDTO> utilisateurs = utilisateurService.findAll();
        return new ResponseEntity<>(utilisateurs, HttpStatus.OK);
    }

    // Variante paginée (?page=0&size=20&sort=...) ; sans "page", la liste complète ci-dessus reste servie
    @GetMapping(params = "page")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE','MEDECIN')")
    public ResponseEntity<PageResponse<UtilisateurResponseDTO>> getAllUtilisateursPage(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(utilisateurService.findAll(pageable));
    }

    @GetMapping("/contacts")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE','MEDECIN')")
    public ResponseEntity<List<Contact>> getAllContacts() {
        List<Contact> contacts = utilisateurService.findAllContacts();
        return new ResponseEntity<>(contacts, HttpStatus.OK);
    }

}
