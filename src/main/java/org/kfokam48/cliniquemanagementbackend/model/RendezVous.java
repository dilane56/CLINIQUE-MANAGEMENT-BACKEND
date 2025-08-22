package org.kfokam48.cliniquemanagementbackend.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;


import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class RendezVous {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime dateRendezVous;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "medecin_id")
    private Medecin medecin;

    private Long secretaireId;

    @ManyToOne
    private TypeRendezVous typeRendezVous;

    private String motif;
    @Enumerated(EnumType.STRING)
    private StatutRendezVous statutRendezVous;
    private LocalDateTime dateTimeFinRendezVousPossible;
    //private int dureeEstimerRendezVousEnMin;

}
