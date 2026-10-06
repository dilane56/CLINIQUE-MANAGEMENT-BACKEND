-- A12 : noms de colonnes harmonisés (les contraintes PK/FK suivent automatiquement le renommage).
-- - medecin_secretaire.secretarire_id (faute de frappe) -> secretaire_id
-- - administrateur.utilisateurs_id et medecin.utilisateurs_id -> utilisateur_id (comme secretaire)
-- Les annotations @JoinTable / @PrimaryKeyJoinColumn des entités sont mises à jour en même temps.

ALTER TABLE medecin_secretaire RENAME COLUMN secretarire_id TO secretaire_id;
ALTER TABLE administrateur RENAME COLUMN utilisateurs_id TO utilisateur_id;
ALTER TABLE medecin RENAME COLUMN utilisateurs_id TO utilisateur_id;
