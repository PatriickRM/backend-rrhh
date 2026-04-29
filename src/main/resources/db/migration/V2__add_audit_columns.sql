-- ══════════════════════════════════════════════════════════════════════════════
-- V2__add_audit_columns.sql
-- Agrega columnas de auditoría a las tablas principales.
-- Corresponde a la clase Auditable.java implementada en Prioridad 3.
-- ══════════════════════════════════════════════════════════════════════════════

-- ── employee ──────────────────────────────────────────────────────────────────
ALTER TABLE `employee`
    ADD COLUMN `created_at`        DATETIME    COMMENT 'Fecha de creación del registro',
    ADD COLUMN `updated_at`        DATETIME    COMMENT 'Fecha de última modificación',
    ADD COLUMN `created_by`        VARCHAR(64) COMMENT 'Usuario que creó el registro',
    ADD COLUMN `last_modified_by`  VARCHAR(64) COMMENT 'Usuario que modificó el registro por última vez';

-- ── department ────────────────────────────────────────────────────────────────
ALTER TABLE `department`
    ADD COLUMN `created_at`        DATETIME,
    ADD COLUMN `updated_at`        DATETIME,
    ADD COLUMN `created_by`        VARCHAR(64),
    ADD COLUMN `last_modified_by`  VARCHAR(64);

-- ── position ──────────────────────────────────────────────────────────────────
ALTER TABLE `position`
    ADD COLUMN `created_at`        DATETIME,
    ADD COLUMN `updated_at`        DATETIME,
    ADD COLUMN `created_by`        VARCHAR(64),
    ADD COLUMN `last_modified_by`  VARCHAR(64);

-- ── leave_requests ────────────────────────────────────────────────────────────
ALTER TABLE `leave_requests`
    ADD COLUMN `created_at`        DATETIME,
    ADD COLUMN `updated_at`        DATETIME,
    ADD COLUMN `created_by`        VARCHAR(64),
    ADD COLUMN `last_modified_by`  VARCHAR(64);
