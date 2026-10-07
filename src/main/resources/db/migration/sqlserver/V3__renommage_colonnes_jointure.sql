-- A12 : noms de colonnes harmonisés (les contraintes PK/FK suivent automatiquement le renommage).
-- - medecin_secretaire.secretarire_id (faute de frappe) -> secretaire_id
-- - administrateur.utilisateurs_id et medecin.utilisateurs_id -> utilisateur_id (comme secretaire)
-- Les annotations @JoinTable / @PrimaryKeyJoinColumn des entités sont mises à jour en même temps.
-- Doit rester équivalent à db/migration/postgresql/V3__renommage_colonnes_jointure.sql.

EXEC sp_rename 'medecin_secretaire.secretarire_id', 'secretaire_id', 'COLUMN';
EXEC sp_rename 'administrateur.utilisateurs_id', 'utilisateur_id', 'COLUMN';
EXEC sp_rename 'medecin.utilisateurs_id', 'utilisateur_id', 'COLUMN';
