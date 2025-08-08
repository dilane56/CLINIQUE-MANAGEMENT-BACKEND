package org.kfokam48.cliniquemanagementbackend.dto.utilisateur;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;
@Data
public class UserUpdatedDTO {


    private String password;
    private String email;
    private String nom;
    private String prenom;
    private String telephone;
    private Roles role;

}
