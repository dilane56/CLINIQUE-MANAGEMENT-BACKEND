package org.kfokam48.cliniquemanagementbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@PrimaryKeyJoinColumn(name = "utilisateurs_id")
@EqualsAndHashCode(callSuper = true)
public class Medecin extends Utilisateur{
    private String specialite;

    @OneToMany(mappedBy = "medecin")
    private List<RendezVous> rendezvous = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name= "medecin_secretaire",
            joinColumns = @JoinColumn(name = "medecin_id"),
            inverseJoinColumns = @JoinColumn(name = "secretarire_id")
    )
    private Set<Secretaire> secretaires = new HashSet<>();

}
