package org.kfokam48.cliniquemanagementbackend.dto.medecin;

import lombok.Data;
import org.kfokam48.cliniquemanagementbackend.dto.RendezVousInUserDto;
import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.UtilisateurResponseDTO;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;


import java.util.List;
@Data
public class MedecinResponseDTO extends UtilisateurResponseDTO {
    private Long id;
    private List<RendezVousInUserDto> rendezvous;
    private String specialite;
    private String role;
    private List<SecretaireInMedecinDTO> secretaires;
}
