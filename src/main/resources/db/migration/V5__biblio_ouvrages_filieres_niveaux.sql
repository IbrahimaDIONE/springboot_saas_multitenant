DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;

CREATE TABLE filieres (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (tenant_id, nom)
);
CREATE INDEX idx_filieres_tenant ON filieres(tenant_id);

CREATE TABLE niveaux (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (tenant_id, nom)
);
CREATE INDEX idx_niveaux_tenant ON niveaux(tenant_id);

CREATE TABLE ouvrages (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    titre VARCHAR(200) NOT NULL,
    auteur VARCHAR(150) NOT NULL,
    resume TEXT NOT NULL,
    filiere_id UUID NOT NULL REFERENCES filieres(id),
    niveau_id UUID NOT NULL REFERENCES niveaux(id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_ouvrages_tenant ON ouvrages(tenant_id);
CREATE INDEX idx_ouvrages_filiere ON ouvrages(filiere_id);
CREATE INDEX idx_ouvrages_niveau ON ouvrages(niveau_id);

INSERT INTO filieres(id, tenant_id, nom, created_at, updated_at) VALUES
('31000000-0000-0000-0000-000000000001','tenant-a','Informatique',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('31000000-0000-0000-0000-000000000002','tenant-b','Lettres modernes',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('31000000-0000-0000-0000-000000000003','tenant-c','Sciences économiques',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);

INSERT INTO niveaux(id, tenant_id, nom, created_at, updated_at) VALUES
('32000000-0000-0000-0000-000000000001','tenant-a','Licence 1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('32000000-0000-0000-0000-000000000002','tenant-b','Master 1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('32000000-0000-0000-0000-000000000003','tenant-c','Doctorat',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);

INSERT INTO ouvrages(id, tenant_id, titre, auteur, resume, filiere_id, niveau_id, created_at, updated_at) VALUES
('20000000-0000-0000-0000-000000000001','tenant-a','Algorithmique fondamentale','Thomas Cormen','Introduction aux algorithmes et à leur analyse.','31000000-0000-0000-0000-000000000001','32000000-0000-0000-0000-000000000001',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000002','tenant-a','Bases de données','Raghu Ramakrishnan','Principes de conception et d’exploitation des bases de données.','31000000-0000-0000-0000-000000000001','32000000-0000-0000-0000-000000000001',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000003','tenant-b','Littérature africaine','Mariama Bâ','Repères pour l’étude des grandes œuvres africaines.','31000000-0000-0000-0000-000000000002','32000000-0000-0000-0000-000000000002',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000004','tenant-b','Poétique et récit','Tzvetan Todorov','Étude des structures narratives et de leurs effets.','31000000-0000-0000-0000-000000000002','32000000-0000-0000-0000-000000000002',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000005','tenant-c','Économie du développement','Amartya Sen','Analyse des capacités, des institutions et du développement humain.','31000000-0000-0000-0000-000000000003','32000000-0000-0000-0000-000000000003',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000006','tenant-c','Microéconomie avancée','Hal R. Varian','Modèles et outils de la microéconomie contemporaine.','31000000-0000-0000-0000-000000000003','32000000-0000-0000-0000-000000000003',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);