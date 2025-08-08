package org.kfokam48.cliniquemanagementbackend.controlleur;


import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.UtilisateurResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;

import org.kfokam48.cliniquemanagementbackend.service.impl.UtilisateurServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/utilisateurs") // URL de base pour le contrôleur
public class UtilisateurController {

    private final UtilisateurServiceImpl utilisateurService;

    public UtilisateurController(UtilisateurServiceImpl utilisateurService) {
        this.utilisateurService = utilisateurService;
    }



    // Endpoint pour récupérer un utilisateur par ID
    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> getUtilisateurById(@PathVariable Long id) {
        Utilisateur utilisateur = utilisateurService.findById(id);
        return new ResponseEntity<>(utilisateur, HttpStatus.OK);
    }

    // Endpoint pour mettre à jour un utilisateur
//    @PutMapping("/{id}")
//    public ResponseEntity<Utilisateur> updateUtilisateur(
//            @PathVariable Long id,
//          @Valid  @RequestBody UtilisateurDTO utilisateurDTO) {
//        Utilisateur utilisateur = utilisateurService.update(id, utilisateurDTO);
//        return new ResponseEntity<>(utilisateur, HttpStatus.OK);
//    }

    // Endpoint pour supprimer un utilisateur par ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUtilisateur(@PathVariable Long id) {
        return utilisateurService.deleteById(id);
    }

    // Endpoint pour récupérer tous les utilisateurs
    @GetMapping
    public ResponseEntity<List<UtilisateurResponseDTO>> getAllUtilisateurs() {
        List<UtilisateurResponseDTO> utilisateurs = utilisateurService.findAll();
        return new ResponseEntity<>(utilisateurs, HttpStatus.OK);
    }
//    @PostMapping("/role/add")
//    public ResponseEntity<Utilisateur> addRoleToUser(@RequestParam Utilisateur user, @RequestParam String roleName) {
//        Utilisateur utilisateurs = utilisateurService.addRoleTouser(user, roleName);
//        return new ResponseEntity<>(utilisateurs, HttpStatus.OK);
//    }
}
