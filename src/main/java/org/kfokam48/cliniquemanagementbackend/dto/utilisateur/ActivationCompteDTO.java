package org.kfokam48.cliniquemanagementbackend.dto.utilisateur;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Désactivation ou réactivation d'un compte du personnel (PATCH /api/utilisateurs/{id}/activation). */
@Data
public class ActivationCompteDTO {

    @NotNull(message = "Le champ actif est obligatoire.")
    private Boolean actif;
}
