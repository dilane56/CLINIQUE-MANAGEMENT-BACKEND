package org.kfokam48.cliniquemanagementbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrescriptionDTO {

    private String medicament;
    @NotNull(message = "Le nom du patient ne doit pas être null")
    @NotBlank(message = "Le nom du patient ne doit pas être vide")
    private Long patientId;
    @NotNull(message = "Le nom du médecin ne doit pas être null")
    @NotBlank(message = "Le nom du médecin ne doit pas être vide")
    private Long medecinId;
    private String instructions;
}
