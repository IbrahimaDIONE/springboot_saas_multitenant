CREATE TABLE memoires (
                          id UUID PRIMARY KEY,
                          tenant_id VARCHAR(50) NOT NULL,
                          titre VARCHAR(255) NOT NULL,
                          auteur VARCHAR(150) NOT NULL,
                          encadreur VARCHAR(150) NOT NULL,
                          resume TEXT NOT NULL,
                          annee INTEGER NOT NULL,
                          filiere_id UUID NOT NULL REFERENCES filieres(id),
                          niveau_id UUID NOT NULL REFERENCES niveaux(id),
                          actif BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMPTZ NOT NULL,
                          updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_memoires_tenant_actif ON memoires(tenant_id, actif);

CREATE TABLE fichiers (
                          id UUID PRIMARY KEY,
                          tenant_id VARCHAR(50) NOT NULL,
                          memoire_id UUID NOT NULL REFERENCES memoires(id) ON DELETE CASCADE,
                          nom_original VARCHAR(255) NOT NULL,
                          chemin_stockage VARCHAR(500) NOT NULL,
                          taille_octets BIGINT NOT NULL,
                          type_mime VARCHAR(100) NOT NULL,
                          disponible BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMPTZ NOT NULL,
                          updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_fichiers_memoire ON fichiers(memoire_id);