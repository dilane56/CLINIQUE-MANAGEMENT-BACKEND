package org.kfokam48.cliniquemanagementbackend.dto.rendezvous;

import lombok.Data;
import org.kfokam48.cliniquemanagementbackend.dto.typeRendezVous.TypeRendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;

import java.time.LocalDateTime;
@Data
public class RendezVousResponseDTO {

    private Long id; // Identifiant du rendez-vous
    private LocalDateTime dateRendezVous; // Date du rendez-vous
    private PatientInRendezVousDTO patient; // Référence au Patient
    private MedecinInRendezVousDto medecin; // Référence au Médecin
    private String motif;// Motif du rendez-vous
    private StatutRendezVous statutRendezVous;
    private LocalDateTime dateTimeFinRendezVousPossible;
    //private int dureeEstimerRendezVousEnMin;
    private TypeRendezVousDTO typeRendezVous; // Référence au Type de Rendez-vous

}
