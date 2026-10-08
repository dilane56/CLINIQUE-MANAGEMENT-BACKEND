-- I5 : désactivation d'un compte du personnel par l'administrateur. Un compte inactif ne peut plus
-- se connecter et ses jetons déjà émis sont refusés. Distinct de "status" (présence dans le chat).
-- Les comptes existants restent actifs (la valeur par défaut remplit les lignes existantes).
-- Doit rester équivalent à db/migration/postgresql/V5__compte_actif.sql.

ALTER TABLE utilisateur ADD actif BIT DEFAULT 1 NOT NULL;
