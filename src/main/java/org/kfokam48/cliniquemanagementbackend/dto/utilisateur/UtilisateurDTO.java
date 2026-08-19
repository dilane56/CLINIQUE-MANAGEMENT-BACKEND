package org.kfokam48.cliniquemanagementbackend.dto.utilisateur;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UtilisateurDTO {

    @NotNull(message = "Password is required")
    @NotBlank(message = "Password cannot be blank")
    private String password;
    @NotNull(message = "Email is required")
    @NotBlank(message = "Email cannot be blank")
    private String email;
    private String nom;
    private String prenom;
    @Size(min = 9, message = "Le numéro de téléphone doit contenir au moins 9 caractères")
    @Size(min = 9, message = "Le numéro de téléphone doit contenir au moins 9 caractères")
    private String telephone;
    private Roles role;






}
