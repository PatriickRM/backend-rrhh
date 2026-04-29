-- ══════════════════════════════════════════════════════════════════════════════
-- V1__initial_schema.sql
-- Schema inicial del sistema RRHH
-- Flyway ejecuta este script una sola vez al primer arranque.
-- Con ddl-auto: validate, Hibernate solo verifica que el schema coincide.
-- ══════════════════════════════════════════════════════════════════════════════

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ── Roles ────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `role` (
    `id`   BIGINT       NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50)  NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Usuarios ─────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `user` (
    `id`        BIGINT        NOT NULL AUTO_INCREMENT,
    `username`  VARCHAR(32)   NOT NULL,
    `password`  VARCHAR(255)  NOT NULL,
    `full_name` VARCHAR(100)  NOT NULL,
    `enabled`   TINYINT(1)    NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Relación Usuario ↔ Roles ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `user_roles` (
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    CONSTRAINT `fk_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `role`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Departamentos ─────────────────────────────────────────────────────────────
-- Nota: id_head_employee se agrega después de crear employee (V1 la deja NULL)
CREATE TABLE IF NOT EXISTS `department` (
    `id_department`    BIGINT       NOT NULL AUTO_INCREMENT,
    `code`             VARCHAR(100) NOT NULL,
    `name`             VARCHAR(100) NOT NULL,
    `description`      VARCHAR(255),
    `enabled`          TINYINT(1)   NOT NULL DEFAULT 1,
    `id_head_employee` BIGINT,                 -- FK circular: se define al final
    PRIMARY KEY (`id_department`),
    UNIQUE KEY `uk_department_code` (`code`),
    UNIQUE KEY `uk_department_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Posiciones ────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `position` (
    `id_position`   BIGINT         NOT NULL AUTO_INCREMENT,
    `title`         VARCHAR(100)   NOT NULL,
    `base_salary`   DECIMAL(10,2),
    `enabled`       TINYINT(1)     NOT NULL DEFAULT 1,
    `id_department` BIGINT         NOT NULL,
    PRIMARY KEY (`id_position`),
    CONSTRAINT `fk_position_department`
        FOREIGN KEY (`id_department`) REFERENCES `department`(`id_department`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Empleados ─────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `employee` (
    `id_employee`       BIGINT        NOT NULL AUTO_INCREMENT,
    `id_user`           BIGINT        NOT NULL,
    `full_name`         VARCHAR(100)  NOT NULL,
    `dni`               VARCHAR(8)    NOT NULL,
    `email`             VARCHAR(200)  NOT NULL,
    `phone`             VARCHAR(15)   NOT NULL,
    `address`           VARCHAR(200)  NOT NULL,
    `date_of_birth`     DATE          NOT NULL,
    `hire_date`         DATE          NOT NULL,
    `contract_end_date` DATE          NOT NULL,
    `id_position`       BIGINT        NOT NULL,
    `id_department`     BIGINT        NOT NULL,
    `salary`            DOUBLE        NOT NULL,
    `status`            VARCHAR(20)   NOT NULL,
    `gender`            VARCHAR(15)   NOT NULL,
    PRIMARY KEY (`id_employee`),
    UNIQUE KEY `uk_employee_dni`   (`dni`),
    UNIQUE KEY `uk_employee_email` (`email`),
    UNIQUE KEY `uk_employee_user`  (`id_user`),
    CONSTRAINT `fk_employee_user`
        FOREIGN KEY (`id_user`)       REFERENCES `user`(`id`),
    CONSTRAINT `fk_employee_position`
        FOREIGN KEY (`id_position`)   REFERENCES `position`(`id_position`),
    CONSTRAINT `fk_employee_department`
        FOREIGN KEY (`id_department`) REFERENCES `department`(`id_department`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- FK circular department → employee (jefe)
ALTER TABLE `department`
    ADD CONSTRAINT `fk_department_head`
    FOREIGN KEY (`id_head_employee`) REFERENCES `employee`(`id_employee`);

-- ── Solicitudes de permiso ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `leave_requests` (
    `id`                   BIGINT        NOT NULL AUTO_INCREMENT,
    `employee_id`          BIGINT        NOT NULL,
    `start_date`           DATE          NOT NULL,
    `end_date`             DATE          NOT NULL,
    `type`                 VARCHAR(25)   NOT NULL,
    `status`               VARCHAR(20)   NOT NULL,
    `justification`        VARCHAR(500)  NOT NULL,
    `evidence_image_path`  VARCHAR(300),
    `reviewed_by_head_name` VARCHAR(150),
    `head_comment`         VARCHAR(400),
    `hr_comment`           VARCHAR(400),
    `request_date`         DATETIME      NOT NULL,
    `head_response_date`   DATETIME,
    `hr_response_date`     DATETIME,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_leave_employee`
        FOREIGN KEY (`employee_id`) REFERENCES `employee`(`id_employee`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Índices de consulta frecuente
CREATE INDEX `idx_leave_employee_status`
    ON `leave_requests` (`employee_id`, `status`);
CREATE INDEX `idx_leave_status`
    ON `leave_requests` (`status`);
CREATE INDEX `idx_leave_dates`
    ON `leave_requests` (`start_date`, `end_date`);
