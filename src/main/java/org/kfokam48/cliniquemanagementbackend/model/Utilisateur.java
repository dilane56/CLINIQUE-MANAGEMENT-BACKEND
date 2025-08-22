package org.kfokam48.cliniquemanagementbackend.model;
import jakarta.persistence.*;

import lombok.Data;

import org.kfokam48.cliniquemanagementbackend.enums.Sexe;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;
import org.kfokam48.cliniquemanagementbackend.enums.UserStatus;

import java.time.Instant;
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
    @Column(nullable = false)
    private String password;
    private String telephone;
    @Enumerated(EnumType.STRING)
    private UserStatus status ; // ACTIVE, INACTIVE, SUSPENDED

    // La date et l'heure de la dernière connexion
    private Instant derniereConnexion;
    @Column(nullable = false)
    private Roles role; // ADMIN, MEDECIN, SECRETAIRE

    public Utilisateur() {
    }
}
