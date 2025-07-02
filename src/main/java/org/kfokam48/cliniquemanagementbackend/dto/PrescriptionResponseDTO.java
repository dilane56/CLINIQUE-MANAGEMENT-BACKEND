package org.kfokam48.cliniquemanagementbackend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PrescriptionResponseDTO {
    private Long id;
    private String medicament;
    private String patientNom;
    private String patientPrenom;
    private String medecinNom;
    private String medecinPrenom;
    private String instructions;
    private LocalDate datePrescription;

    // Getters et Setters
}
