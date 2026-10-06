package org.kfokam48.cliniquemanagementbackend.dto.facture;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureDTO;

import java.util.List;

@Data
public class FactureDTO {

    @NotNull(message = "L'id du rendez-vous ne doit pas être nul")
    private Long rendezVousId;
    @Valid
    private List<LigneFactureDTO> lignesFacture;

    // Getters et Setters
}
