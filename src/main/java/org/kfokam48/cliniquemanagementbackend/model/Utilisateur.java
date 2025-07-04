package org.kfokam48.cliniquemanagementbackend.model;
import jakarta.persistence.*;

import lombok.Data;

import org.kfokam48.cliniquemanagementbackend.enums.Sexe;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;

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

    @Column(nullable = false)
    private Roles role; // ADMIN, MEDECIN, INFIRMIERE, SECRETAIRE

    public Utilisateur() {
    }
}
