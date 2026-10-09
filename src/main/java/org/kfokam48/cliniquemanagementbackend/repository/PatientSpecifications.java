package org.kfokam48.cliniquemanagementbackend.repository;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.springframework.data.jpa.domain.Specification;

/**
 * Recherche de patients (GET /api/patients/recherche), par exemple pour choisir le patient d'un
 * rendez-vous sans charger toute la liste.
 */
public final class PatientSpecifications {

    private PatientSpecifications() {
    }

    /**
     * Texte cherché (insensible à la casse) dans "nom prénom", "prénom nom", l'e-mail ou le
     * téléphone. Texte absent ou vide : aucun filtre.
     */
    public static Specification<Patient> rechercher(String texte, Long medecinId) {
        return Specification.where(rechercher(texte)).and(duMedecin(medecinId));
    }

    // Patients qui ont au moins un rendez-vous avec ce médecin
    static Specification<Patient> duMedecin(Long medecinId) {
        if (medecinId == null) {
            return null;
        }
        return (racine, requete, cb) -> {
            Subquery<Long> rendezVous = requete.subquery(Long.class);
            Root<RendezVous> r = rendezVous.from(RendezVous.class);
            rendezVous.select(r.get("id")).where(cb.equal(r.get("patient"), racine), cb.equal(r.get("medecin").get("id"), medecinId));
            return cb.exists(rendezVous);
        };
    }

    public static Specification<Patient> rechercher(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        String motif = RendezVousSpecifications.motifContient(texte);
        return (racine, requete, cb) -> {
            Expression<String> nom = cb.coalesce(racine.get("nom"), "");
            Expression<String> prenom = cb.coalesce(racine.get("prenom"), "");
            return cb.or(
                    cb.like(cb.lower(cb.concat(cb.concat(nom, " "), prenom)), motif, '\\'),
                    cb.like(cb.lower(cb.concat(cb.concat(prenom, " "), nom)), motif, '\\'),
                    cb.like(cb.lower(cb.coalesce(racine.get("email"), "")), motif, '\\'),
                    cb.like(cb.lower(cb.coalesce(racine.get("telephone"), "")), motif, '\\'));
        };
    }
}
