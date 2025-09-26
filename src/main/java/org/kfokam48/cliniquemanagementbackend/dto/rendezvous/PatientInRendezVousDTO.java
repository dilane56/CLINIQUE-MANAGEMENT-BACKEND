package org.kfokam48.cliniquemanagementbackend.dto.rendezvous;

import lombok.Data;

@Data
public class PatientInRendezVousDTO {

    private Long id;
    private Integer numeroDossierMedical;
    private String nom;
    private String email;
    private String prenom;
}
