-- ================================================
-- V6 : Module Emprunt numérique
-- Auteur : Khadidiatou Ba
-- Branche : feature/emprunt-lecture
-- ================================================

-- Ajout colonne url_fichier sur ouvrages
ALTER TABLE ouvrages ADD COLUMN IF NOT EXISTS url_fichier VARCHAR(2048);

CREATE TABLE emprunts (
                          id              UUID PRIMARY KEY,
                          tenant_id       VARCHAR(50)  NOT NULL,
                          etudiant_id     UUID         NOT NULL REFERENCES etudiants(id),
                          ouvrage_id      UUID         NOT NULL REFERENCES ouvrages(id),
                          date_emprunt    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          date_expiration TIMESTAMPTZ  NOT NULL,
                          statut          VARCHAR(30)  NOT NULL DEFAULT 'ACTIF',
                          created_at      TIMESTAMPTZ  NOT NULL,
                          updated_at      TIMESTAMPTZ  NOT NULL,
                          CONSTRAINT chk_statut_emprunt
                              CHECK (statut IN ('ACTIF','EXPIRE','RETARDE'))
);

CREATE INDEX idx_emprunts_tenant   ON emprunts(tenant_id);
CREATE INDEX idx_emprunts_etudiant ON emprunts(etudiant_id);
CREATE INDEX idx_emprunts_ouvrage  ON emprunts(ouvrage_id);
CREATE INDEX idx_emprunts_statut   ON emprunts(statut);