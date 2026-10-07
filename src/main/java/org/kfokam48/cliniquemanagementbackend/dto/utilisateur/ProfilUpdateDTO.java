package org.kfokam48.cliniquemanagementbackend.dto.utilisateur;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Modification de son propre profil (PUT /api/utilisateurs/me).
 * Volontairement limité : l'e-mail (identifiant de connexion), le rôle et le mot de passe
 * ne sont pas modifiables par cette voie.
 */
@Data
public class ProfilUpdateDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
    private String prenom;

    @Size(min = 9, max = 20, message = "Le numéro de téléphone doit contenir entre 9 et 20 caractères")
    private String telephone;

    @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
    private String adresse;
}
