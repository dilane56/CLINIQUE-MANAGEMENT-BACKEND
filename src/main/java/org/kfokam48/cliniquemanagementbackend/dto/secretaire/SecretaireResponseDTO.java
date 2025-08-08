package org.kfokam48.cliniquemanagementbackend.dto.secretaire;

import lombok.Data;
import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.UtilisateurResponseDTO;

import java.util.List;

@Data
public class SecretaireResponseDTO extends UtilisateurResponseDTO {
    private List<MedecinInSecretaireDTO> medecins;
}
