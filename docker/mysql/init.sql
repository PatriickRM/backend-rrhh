-- ──────────────────────────────────────────────────────────────────────────
-- Script de inicialización para el sistema RRHH
-- Se ejecuta una sola vez cuando el contenedor MySQL se crea por primera vez
-- ──────────────────────────────────────────────────────────────────────────

-- Datos semilla: Roles del sistema
INSERT IGNORE INTO `role` (`name`) VALUES
    ('CHRO'),
    ('HEAD'),
    ('EMPLOYEE');

-- Usuario administrador inicial (contraseña: Admin1234! — cámbiala en producción)
-- Hash BCrypt con strength 12 de "Admin1234!"
INSERT IGNORE INTO `user` (`username`, `full_name`, `password`, `enabled`) VALUES
    ('admin', 'Administrador RRHH',
     '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- Admin1234!
     true);

-- Asignar rol CHRO al admin
INSERT IGNORE INTO `user_roles` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `user` u, `role` r
WHERE u.username = 'admin' AND r.name = 'CHRO';
