package org.kfokam48.cliniquemanagementbackend.dto.prescription;

import lombok.Data;
import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionDTO;

import java.time.LocalDate;
import java.util.List;

@Data
public class PrescriptionDTO {
    private String description;
    private Long rendezVousId;
    private List<LignePrescriptionDTO> lignes;

}
