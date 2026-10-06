package org.kfokam48.cliniquemanagementbackend.rendezvous;

import org.kfokam48.cliniquemanagementbackend.enums.Roles;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.kfokam48.cliniquemanagementbackend.model.TypeRendezVous;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Constructeurs d'entités minimales pour les tests de rendez-vous
final class RendezVousTestData {

    private RendezVousTestData() {
    }

    static Medecin medecin(String email) {
        Medecin medecin = new Medecin();
        medecin.setEmail(email);
        medecin.setNom("Medecin");
        medecin.setPassword("hash");
        medecin.setRole(Roles.MEDECIN);
        return medecin;
    }

    static Patient patient(String email) {
        Patient patient = new Patient();
        patient.setEmail(email);
        patient.setNom("Patient");
        return patient;
    }

    static TypeRendezVous consultation30Minutes() {
        return new TypeRendezVous(null, "Consultation générale", 30, new BigDecimal("15000.00"));
    }

    static RendezVous rendezVous(Medecin medecin, Patient patient, TypeRendezVous type,
                                 LocalDateTime debut, StatutRendezVous statut) {
        RendezVous rendezVous = new RendezVous();
        rendezVous.setMedecin(medecin);
        rendezVous.setPatient(patient);
        rendezVous.setTypeRendezVous(type);
        rendezVous.setDateRendezVous(debut);
        rendezVous.setDateTimeFinRendezVousPossible(debut.plusMinutes(type.getDuree()));
        rendezVous.setStatutRendezVous(statut);
        return rendezVous;
    }
}
