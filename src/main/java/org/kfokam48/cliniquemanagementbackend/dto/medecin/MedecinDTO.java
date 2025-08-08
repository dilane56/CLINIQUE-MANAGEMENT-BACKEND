package org.kfokam48.cliniquemanagementbackend.dto.medecin;

import lombok.Data;
import org.kfokam48.cliniquemanagementbackend.dto.utilisateur.UtilisateurDTO;


@Data
//@EqualsAndHashCode(callSuper = true)

public class MedecinDTO extends UtilisateurDTO {

    private String specialite;

    // Getters et Setters
}

