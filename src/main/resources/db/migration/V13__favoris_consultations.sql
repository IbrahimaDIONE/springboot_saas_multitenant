CREATE TABLE favoris (
                         id UUID PRIMARY KEY,
                         tenant_id VARCHAR(50) NOT NULL,
                         etudiant_id UUID NOT NULL REFERENCES etudiants(id),
                         type_ressource VARCHAR(20) NOT NULL,
                         ressource_id UUID NOT NULL,
                         created_at TIMESTAMPTZ NOT NULL,
                         updated_at TIMESTAMPTZ NOT NULL,
                         CONSTRAINT chk_type_ressource_favori CHECK (type_ressource IN ('OUVRAGE','MEMOIRE')),
                         CONSTRAINT uq_favori UNIQUE (etudiant_id, type_ressource, ressource_id)
);
CREATE INDEX idx_favoris_etudiant ON favoris(etudiant_id);

CREATE TABLE consultations (
                               id UUID PRIMARY KEY,
                               tenant_id VARCHAR(50) NOT NULL,
                               etudiant_id UUID NOT NULL REFERENCES etudiants(id),
                               type_ressource VARCHAR(20) NOT NULL,
                               ressource_id UUID NOT NULL,
                               date_consultation TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               created_at TIMESTAMPTZ NOT NULL,
                               updated_at TIMESTAMPTZ NOT NULL,
                               CONSTRAINT chk_type_ressource_consultation CHECK (type_ressource IN ('OUVRAGE','MEMOIRE'))
);
CREATE INDEX idx_consultations_etudiant ON consultations(etudiant_id, date_consultation DESC);