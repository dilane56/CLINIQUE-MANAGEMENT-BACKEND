package org.kfokam48.cliniquemanagementbackend.dto.prescription;

import lombok.Data;
import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionResponseDTO;

import java.time.LocalDate;
import java.util.List;

@Data
public class PrescriptionResponseDTO {
    private Long id;
    private String description;
    private String patientNom;
    private String patientPrenom;
    private String medecinNom;
    private String medecinPrenom;
    private LocalDate date;
    private List<LignePrescriptionResponseDTO> lignes;

    // Getters et Setters
}
