package org.kfokam48.cliniquemanagementbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class RendezVousInUserDto {
    private Long id;
    private LocalDateTime dateRendezVous; // Date du rendez-vous
    private String patientNom; // Référence au Patient
    private String patientPrenom;
    private String medecinNom;
    private String medecinPrenon;// Référence au Médecin
    private String motif;
}
