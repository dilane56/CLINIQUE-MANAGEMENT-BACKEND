package org.kfokam48.cliniquemanagementbackend.dto.rendezvous;

import lombok.Data;

@Data
public class MedecinInRendezVousDto {
    private Long id;
    private String nom;
    private String prenom;
    private String specialite;


}
