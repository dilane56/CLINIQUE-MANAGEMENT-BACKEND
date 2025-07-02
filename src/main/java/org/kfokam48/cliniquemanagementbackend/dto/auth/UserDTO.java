package org.kfokam48.cliniquemanagementbackend.dto.auth;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private String telephone;
    private String sexe;
    private String adresse;
//    private String role;
}
