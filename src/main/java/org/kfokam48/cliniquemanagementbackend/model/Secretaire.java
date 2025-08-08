package org.kfokam48.cliniquemanagementbackend.model;


import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;


@AllArgsConstructor
@Data
@Entity
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "utilisateur_id")
public class Secretaire extends Utilisateur{

    @ManyToMany(mappedBy = "secretaires")
    private Set<Medecin> medecins = new HashSet<>();
}
