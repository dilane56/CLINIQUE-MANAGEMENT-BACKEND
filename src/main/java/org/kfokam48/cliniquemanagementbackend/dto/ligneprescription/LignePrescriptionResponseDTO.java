package org.kfokam48.cliniquemanagementbackend.dto.ligneprescription;

import lombok.Data;

@Data
public class LignePrescriptionResponseDTO {
    private Long id;
    private String medicament;
    private String dosage;
    private String frequence;
    private int duree;
    private Long prescriptionId;
}
