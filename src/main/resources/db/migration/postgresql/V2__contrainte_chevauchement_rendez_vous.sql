-- Seconde barrière contre les doubles réservations (I10), en plus du verrou applicatif
-- (SELECT ... FOR UPDATE sur le médecin dans RendezVousServiceImpl).
-- La base refuse deux rendez-vous actifs du même médecin dont les créneaux se chevauchent.
-- Créneau semi-ouvert [début, fin) : des rendez-vous qui se suivent (10h00-10h30 puis 10h30) restent permis.
-- Les statuts ANNULER, A_REPROGRAMMER et EXPIRE libèrent le créneau
-- (même liste que RendezVousServiceImpl.STATUTS_LIBERANT_LE_CRENEAU : à garder synchronisées).
--
-- PostgreSQL uniquement : SQL Server n'a pas de contrainte d'exclusion (le verrou applicatif suffit en dev).
-- Nécessite l'extension btree_gist (disponible sur Neon, Render et Railway).

CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE rendez_vous
    ADD CONSTRAINT ex_rendez_vous_medecin_chevauchement
    EXCLUDE USING gist (
        medecin_id WITH =,
        tsrange(date_rendez_vous, date_time_fin_rendez_vous_possible, '[)') WITH &&
    )
    WHERE (statut_rendez_vous NOT IN ('ANNULER', 'A_REPROGRAMMER', 'EXPIRE'));
