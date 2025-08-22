package org.kfokam48.cliniquemanagementbackend.dto.rendezvous;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RendezVousUpdateDto {
    @NotNull(message = "La date du rendez-vous ne doit pas être null")
    private LocalDateTime dateRendezVous;// Date du rendez-vous
    @NotNull(message = "Le nom du patient ne doit pas être null")
    private Long patientId; // Référence au Patient
    @NotNull(message = "Le nom du médecin ne doit pas être null")
    private Long medecinId; // Référence au Médecin
    private String motif; // Motif du rendez-vous
    //private Integer dureeEstimerRendezVousEnMin;
    private Long typeRendezVousId; // Référence au Type de Rendez-vous

}
