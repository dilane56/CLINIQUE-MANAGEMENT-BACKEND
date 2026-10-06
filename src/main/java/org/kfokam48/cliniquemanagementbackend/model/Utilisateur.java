package org.kfokam48.cliniquemanagementbackend.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import jakarta.validation.constraints.Size;
import lombok.Data;

import org.kfokam48.cliniquemanagementbackend.enums.Sexe;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;
import org.kfokam48.cliniquemanagementbackend.enums.UserStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data


@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;
    private String nom;
    private String prenom;
    // Jamais renvoyé dans une réponse JSON
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;
    @Size(min = 9, message = "Le numéro de téléphone doit contenir au moins 9 caractères")
    @Column(length = 20)
    private String telephone;
    @Enumerated(EnumType.STRING)
    private UserStatus status ; // ACTIVE, INACTIVE, SUSPENDED

    // La date et l'heure de la dernière connexion
    private Instant derniereConnexion;
    @Column(nullable = false)
    private Roles role; // ADMIN, MEDECIN, SECRETAIRE
    private LocalDate dateCreation;
    private String adresse;

    public Utilisateur() {
    }
}
