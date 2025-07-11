package org.kfokam48.cliniquemanagementbackend.model;


import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@Data
@Entity
@PrimaryKeyJoinColumn(name = "utilisateur_id")
public class Secretaire extends Utilisateur{

    @ManyToMany(mappedBy = "secretaires")
    private List<Medecin> medecins = new ArrayList<>();
}
