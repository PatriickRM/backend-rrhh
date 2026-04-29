-- ══════════════════════════════════════════════════════════════════════════════
-- V3__seed_roles_and_admin.sql
-- Datos iniciales obligatorios para que el sistema funcione.
-- Se ejecuta en todo entorno: dev, staging, producción.
-- ══════════════════════════════════════════════════════════════════════════════

-- ── Roles del sistema ─────────────────────────────────────────────────────────
INSERT INTO `role` (`name`) VALUES
    ('CHRO'),       -- Director de RRHH: acceso total
    ('HEAD'),       -- Jefe de departamento: gestiona su equipo
    ('EMPLOYEE');   -- Empleado regular

-- ── Usuario administrador inicial ────────────────────────────────────────────
-- Contraseña por defecto: Admin1234!
-- Hash BCrypt strength=12 —
INSERT INTO `user` (`username`, `full_name`, `password`, `enabled`) VALUES (
    'admin',
    'Administrador RRHH',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCakfP0VbNWnLhqnmFbG9gK',
    1
);

-- Asignar rol CHRO al admin
INSERT INTO `user_roles` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `user` u
CROSS JOIN `role` r
WHERE u.username = 'admin'
  AND r.name = 'CHRO';
