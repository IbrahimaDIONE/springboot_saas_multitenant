-- ================================================
-- V10 : Module Notifications et Pénalités
-- Auteur : Khadidiatou Ba
-- Branche : feature/notifications-penalites
-- ================================================

CREATE TABLE notifications (
                               id          UUID PRIMARY KEY,
                               tenant_id   VARCHAR(50)  NOT NULL,
                               etudiant_id UUID         NOT NULL REFERENCES etudiants(id),
                               message     TEXT         NOT NULL,
                               type        VARCHAR(50)  NOT NULL,
                               lu          BOOLEAN      NOT NULL DEFAULT FALSE,
                               created_at  TIMESTAMPTZ  NOT NULL,
                               updated_at  TIMESTAMPTZ  NOT NULL,
                               CONSTRAINT chk_type_notification
                                   CHECK (type IN (
                                                   'NOUVELLE_RESSOURCE',
                                                   'RAPPEL_ECHEANCE',
                                                   'AVERTISSEMENT_RETARD',
                                                   'PENALITE'
                                       ))
);

CREATE INDEX idx_notifications_tenant   ON notifications(tenant_id);
CREATE INDEX idx_notifications_etudiant ON notifications(etudiant_id);
CREATE INDEX idx_notifications_lu       ON notifications(lu);
CREATE INDEX idx_notifications_type     ON notifications(type);

CREATE TABLE regles_penalite (
                                 id               UUID PRIMARY KEY,
                                 tenant_id        VARCHAR(50) NOT NULL,
                                 jours_tolerance  INT         NOT NULL DEFAULT 0,
                                 type_consequence VARCHAR(50) NOT NULL DEFAULT 'AVERTISSEMENT',
                                 description      TEXT,
                                 created_at       TIMESTAMPTZ NOT NULL,
                                 updated_at       TIMESTAMPTZ NOT NULL,
                                 CONSTRAINT chk_type_consequence
                                     CHECK (type_consequence IN (
                                                                 'AVERTISSEMENT',
                                                                 'SUSPENSION_TEMPORAIRE'
                                         ))
);

CREATE INDEX idx_regles_penalite_tenant ON regles_penalite(tenant_id);

CREATE TABLE penalites (
                           id               UUID         PRIMARY KEY,
                           tenant_id        VARCHAR(50)  NOT NULL,
                           etudiant_id      UUID         NOT NULL REFERENCES etudiants(id),
                           emprunt_id       UUID         NOT NULL REFERENCES emprunts(id),
                           motif            TEXT         NOT NULL,
                           date_application TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           consequence      VARCHAR(50)  NOT NULL,
                           created_at       TIMESTAMPTZ  NOT NULL,
                           updated_at       TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_penalites_tenant   ON penalites(tenant_id);
CREATE INDEX idx_penalites_etudiant ON penalites(etudiant_id);
CREATE INDEX idx_penalites_emprunt  ON penalites(emprunt_id);