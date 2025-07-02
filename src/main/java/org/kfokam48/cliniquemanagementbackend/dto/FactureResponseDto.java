package org.kfokam48.cliniquemanagementbackend.dto;

import lombok.Data;
import org.kfokam48.cliniquemanagementbackend.enums.ModePayement;
import org.kfokam48.cliniquemanagementbackend.enums.StatutPayement;
import java.time.LocalDateTime;


@Data
public class FactureResponseDto {
    private Long id;
    private Double montantTotal;
    private LocalDateTime dateEmission;
    private String patientEmail;
    private String rendezvousMotif;
    private LocalDateTime datePayement;
    private double montantVerser;
    private double montantRestant;
    private StatutPayement statutPayement;
    private ModePayement modePayement;
    private String medecinEmail;
}
