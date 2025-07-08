package org.kfokam48.cliniquemanagementbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kfokam48.cliniquemanagementbackend.enums.Sexe;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;
    private String nom;
    private String prenom;
    private String telephone;
    private Date dateNaissance;
    private String antecedents;
    private String allergies;
    private Sexe sexe;
    private String adresse;

    @OneToMany(mappedBy = "patient")
    private List<RendezVous> rendezvous = new ArrayList<>();
}
