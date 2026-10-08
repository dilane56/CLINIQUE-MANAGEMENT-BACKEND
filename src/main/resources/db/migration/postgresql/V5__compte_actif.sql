-- I5 : désactivation d'un compte du personnel par l'administrateur. Un compte inactif ne peut plus
-- se connecter et ses jetons déjà émis sont refusés. Distinct de "status" (présence dans le chat).
-- Les comptes existants restent actifs.
-- Doit rester équivalent à db/migration/sqlserver/V5__compte_actif.sql.

ALTER TABLE utilisateur ADD COLUMN actif BOOLEAN DEFAULT TRUE NOT NULL;
