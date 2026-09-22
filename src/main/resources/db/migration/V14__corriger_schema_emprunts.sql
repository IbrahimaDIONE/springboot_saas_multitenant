ALTER TABLE emprunts ADD COLUMN IF NOT EXISTS date_retour TIMESTAMPTZ;

ALTER TABLE emprunts DROP CONSTRAINT IF EXISTS chk_statut_emprunt;
ALTER TABLE emprunts ADD CONSTRAINT chk_statut_emprunt
    CHECK (statut IN ('ACTIF', 'EXPIRE', 'RETARDE', 'RETOURNE'));