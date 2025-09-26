package org.kfokam48.cliniquemanagementbackend.dto.profile;

import lombok.Data;

@Data
public class ProfileDTO {
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String adresse;
}
