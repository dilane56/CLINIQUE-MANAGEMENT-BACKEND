package org.kfokam48.cliniquemanagementbackend.dto.patient;

import lombok.Data;
import org.kfokam48.cliniquemanagementbackend.dto.RendezVousInUserDto;
import org.kfokam48.cliniquemanagementbackend.dto.UtilisateurDTO;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;
import org.kfokam48.cliniquemanagementbackend.enums.Sexe;


import java.util.Date;
import java.util.List;

@Data
public class PatientResponseDTO  {
    private Long id;
    private Date dateNaissance;
    private List<RendezVousInUserDto> rendezvous;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String antecedents;
    private String allergies;
    private Sexe sexe;
    private String adresse;

}
