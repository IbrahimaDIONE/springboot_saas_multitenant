ALTER TABLE ouvrages ADD COLUMN categorie_id UUID REFERENCES categories(id);

ALTER TABLE emprunts ADD COLUMN date_retour TIMESTAMPTZ;
ALTER TABLE emprunts DROP CONSTRAINT chk_statut_emprunt;
ALTER TABLE emprunts ADD CONSTRAINT chk_statut_emprunt
    CHECK (statut IN ('ACTIF','EXPIRE','RETARDE','RETOURNE'));
