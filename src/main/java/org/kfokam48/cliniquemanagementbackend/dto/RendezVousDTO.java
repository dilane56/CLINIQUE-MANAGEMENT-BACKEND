package org.kfokam48.cliniquemanagementbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


import java.time.LocalDateTime;

@Data
public class RendezVousDTO {

    @NotNull(message = "La date du rendez-vous ne doit pas être null")
    private LocalDateTime dateRendezVous;// Date du rendez-vous
    @NotNull(message = "Le nom du patient ne doit pas être null")
    @NotBlank(message = "Le nom du patient ne doit pas être vide")
    private Long patientId; // Référence au Patient
    @NotNull(message = "Le nom du médecin ne doit pas être null")
    @NotBlank(message = "Le nom du médecin ne doit pas être vide")
    private Long medecinId; // Référence au Médecin
    private String motif; // Motif du rendez-vous

    // Getters et Setters
}

