package org.kfokam48.cliniquemanagementbackend.repository;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.kfokam48.cliniquemanagementbackend.model.TypeRendezVous;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Critères de recherche des rendez-vous (GET /api/rendezvous/recherche).
 * Chaque critère absent (null ou vide) est ignoré. Des Specifications plutôt qu'une requête
 * "(:param IS NULL OR ...)" : avec un paramètre null, PostgreSQL ne sait pas typer la requête.
 */
public final class RendezVousSpecifications {

    private RendezVousSpecifications() {
    }

    public static Specification<RendezVous> rechercher(String texte, StatutRendezVous statut, LocalDate date) {
        return Specification.where(contientTexte(texte))
                .and(aLeStatut(statut))
                .and(estLe(date));
    }

    /**
     * Texte cherché (insensible à la casse) dans le nom ou le prénom du patient ou du médecin,
     * "nom prénom" ou "prénom nom", ou le libellé du type de rendez-vous.
     */
    static Specification<RendezVous> contientTexte(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        String motif = "%" + texte.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        return (racine, requete, cb) -> {
            Join<RendezVous, Patient> patient = racine.join("patient");
            Join<RendezVous, Medecin> medecin = racine.join("medecin");
            Join<RendezVous, TypeRendezVous> type = racine.join("typeRendezVous");
            Expression<String> patientNom = cb.coalesce(patient.get("nom"), "");
            Expression<String> patientPrenom = cb.coalesce(patient.get("prenom"), "");
            Expression<String> medecinNom = cb.coalesce(medecin.get("nom"), "");
            Expression<String> medecinPrenom = cb.coalesce(medecin.get("prenom"), "");
            return cb.or(
                    cb.like(cb.lower(cb.concat(cb.concat(patientNom, " "), patientPrenom)), motif, '\\'),
                    cb.like(cb.lower(cb.concat(cb.concat(patientPrenom, " "), patientNom)), motif, '\\'),
                    cb.like(cb.lower(cb.concat(cb.concat(medecinNom, " "), medecinPrenom)), motif, '\\'),
                    cb.like(cb.lower(cb.concat(cb.concat(medecinPrenom, " "), medecinNom)), motif, '\\'),
                    cb.like(cb.lower(cb.coalesce(type.get("libelle"), "")), motif, '\\'));
        };
    }

    static Specification<RendezVous> aLeStatut(StatutRendezVous statut) {
        return statut == null ? null : (racine, requete, cb) -> cb.equal(racine.get("statutRendezVous"), statut);
    }

    // Rendez-vous de la journée [date 00:00, date+1 00:00[
    static Specification<RendezVous> estLe(LocalDate date) {
        return date == null ? null : (racine, requete, cb) -> cb.and(
                cb.greaterThanOrEqualTo(racine.get("dateRendezVous"), date.atStartOfDay()),
                cb.lessThan(racine.get("dateRendezVous"), date.plusDays(1).atStartOfDay()));
    }
}
