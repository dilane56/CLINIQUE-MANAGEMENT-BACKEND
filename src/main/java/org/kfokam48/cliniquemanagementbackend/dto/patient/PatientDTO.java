package org.kfokam48.cliniquemanagementbackend.dto.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.kfokam48.cliniquemanagementbackend.enums.Sexe;


import java.time.LocalDate;
import java.util.Date;

@Data
public class PatientDTO  {


   @NotNull(message = "Email is required")
   @NotBlank(message = "Email cannot be blank")
   private String email;
   private String nom;
   private String prenom;
   @Size(min = 9, message = "Le numéro de téléphone doit contenir au moins 9 caractères")
   private String telephone;
   private LocalDate dateNaissance;
   private String antecedents;
   private String allergies;
   private Sexe sexe;
   private String adresse;


}
