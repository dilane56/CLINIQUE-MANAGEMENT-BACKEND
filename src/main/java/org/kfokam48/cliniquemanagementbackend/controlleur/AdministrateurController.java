package org.kfokam48.cliniquemanagementbackend.controlleur;


import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.AdministrateurDTO;
import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.UtilisateurResponseDTO;
import org.kfokam48.cliniquemanagementbackend.mapper.UtilisateurMapper;
import org.kfokam48.cliniquemanagementbackend.model.Administrateur;
import org.kfokam48.cliniquemanagementbackend.service.AdministrateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/administrateurs")
public class AdministrateurController {
    private final AdministrateurService administrateurService;
    private final UtilisateurMapper utilisateurMapper;

    public AdministrateurController(AdministrateurService administrateurService, UtilisateurMapper utilisateurMapper) {
        this.administrateurService = administrateurService;
        this.utilisateurMapper = utilisateurMapper;
    }


    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDTO> createAdministrateur(@Valid @RequestBody AdministrateurDTO administrateurDTO) {
        Administrateur administrateur = administrateurService.save(administrateurDTO);
        return ResponseEntity.ok(utilisateurMapper.utilisateurToUtilisateurResponseDTO(administrateur));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UtilisateurResponseDTO>> getAllAdministrateurs() {
        List<UtilisateurResponseDTO> administrateurs = administrateurService.findAll().stream()
                .map(utilisateurMapper::utilisateurToUtilisateurResponseDTO)
                .toList();
        return ResponseEntity.ok(administrateurs);
    }

    // Variante paginée (?page=0&size=20&sort=...) ; sans "page", la liste complète ci-dessus reste servie
    @GetMapping(value = "/all", params = "page")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UtilisateurResponseDTO>> getAllAdministrateursPage(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(administrateurService.findAll(pageable), admins -> admins.stream().map(utilisateurMapper::utilisateurToUtilisateurResponseDTO).toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDTO> getAdministrateurById(@PathVariable Long id) {
        Administrateur administrateur = administrateurService.findById(id);
        return ResponseEntity.ok(utilisateurMapper.utilisateurToUtilisateurResponseDTO(administrateur));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDTO> updateAdministrateur(@PathVariable Long id,@Valid @RequestBody AdministrateurDTO administrateurDTO) {
        Administrateur updatedAdministrateur = administrateurService.update(id, administrateurDTO);
        return ResponseEntity.ok(utilisateurMapper.utilisateurToUtilisateurResponseDTO(updatedAdministrateur));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDTO> getAdministrateurByEmail(@PathVariable String email) {
        Administrateur administrateur = administrateurService.findByEmail(email);
        return ResponseEntity.ok(utilisateurMapper.utilisateurToUtilisateurResponseDTO(administrateur));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteAdministrateur(@PathVariable Long id) {
        administrateurService.deleteById(id);
        return ResponseEntity.ok("Administrateur deleted successfully");
    }


}

