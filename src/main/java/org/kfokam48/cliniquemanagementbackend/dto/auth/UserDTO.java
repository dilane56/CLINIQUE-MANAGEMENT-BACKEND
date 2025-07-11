package org.kfokam48.cliniquemanagementbackend.dto.auth;

import lombok.Data;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private String telephone;
    private String sexe;
    private String adresse;
    private Roles role;
//    private String role;
}
