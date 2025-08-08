package org.kfokam48.cliniquemanagementbackend.dto.prescription;

import lombok.Data;

import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionUpdateDTO;

import java.util.ArrayList;
import java.util.List;
@Data
public class PrescriptionUpdateDTO {
    private String description;
    private Long rendezVousId;
    private List<LignePrescriptionUpdateDTO> lignes ;

}
