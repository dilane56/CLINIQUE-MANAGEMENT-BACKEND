package org.kfokam48.cliniquemanagementbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class LignePrescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String medicament;
    private String dosage;
    private String frequence;
    private int duree; // en jours

    @ManyToOne
    private Prescription prescription;
}

