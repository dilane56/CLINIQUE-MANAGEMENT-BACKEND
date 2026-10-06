package org.kfokam48.cliniquemanagementbackend.controlleur;


import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.AdministrateurDTO;
import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.UtilisateurResponseDTO;
import org.kfokam48.cliniquemanagementbackend.mapper.UtilisateurMapper;
import org.kfokam48.cliniquemanagementbackend.model.Administrateur;
import org.kfokam48.cliniquemanagementbackend.service.impl.AdministrateurServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/administrateurs")
public class AdministrateurController {
    private final AdministrateurServiceImpl administrateurService;
    private final UtilisateurMapper utilisateurMapper;

    public AdministrateurController(AdministrateurServiceImpl administrateurService, UtilisateurMapper utilisateurMapper) {
        this.administrateurService = administrateurService;
        this.utilisateurMapper = utilisateurMapper;
    }


    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')") // Accès pour les rôles ADMIN
    public ResponseEntity<UtilisateurResponseDTO> createAdministrateur(@Valid @RequestBody AdministrateurDTO administrateurDTO) {
        Administrateur administrateur = administrateurService.save(administrateurDTO);
        return ResponseEntity.ok(utilisateurMapper.utilisateurToUtilisateurResponseDTO(administrateur));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')") // Accès pour les rôles ADMIN
    public ResponseEntity<List<UtilisateurResponseDTO>> getAllAdministrateurs() {
        List<UtilisateurResponseDTO> administrateurs = administrateurService.findAll().stream()
                .map(utilisateurMapper::utilisateurToUtilisateurResponseDTO)
                .toList();
        return ResponseEntity.ok(administrateurs);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Accès pour les rôles  ADMIN
    public ResponseEntity<UtilisateurResponseDTO> getAdministrateurById(@PathVariable Long id) {
        Administrateur administrateur = administrateurService.findById(id);
        return ResponseEntity.ok(utilisateurMapper.utilisateurToUtilisateurResponseDTO(administrateur));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UtilisateurResponseDTO> updateAdministrateur(@PathVariable Long id,@Valid @RequestBody AdministrateurDTO administrateurDTO) {
        Administrateur updatedAdministrateur = administrateurService.update(id, administrateurDTO);
        return ResponseEntity.ok(utilisateurMapper.utilisateurToUtilisateurResponseDTO(updatedAdministrateur));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UtilisateurResponseDTO> getAdministrateurByEmail(@PathVariable String email) {
        Administrateur administrateur = administrateurService.findByEmail(email);
        return ResponseEntity.ok(utilisateurMapper.utilisateurToUtilisateurResponseDTO(administrateur));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteAdministrateur(@PathVariable Long id) {
        return administrateurService.deleteById(id);
    }


}

