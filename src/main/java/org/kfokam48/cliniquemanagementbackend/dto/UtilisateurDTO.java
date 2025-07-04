package org.kfokam48.cliniquemanagementbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;
import org.kfokam48.cliniquemanagementbackend.enums.Sexe;

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





}
