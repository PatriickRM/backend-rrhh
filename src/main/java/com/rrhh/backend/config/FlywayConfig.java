package com.rrhh.backend.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
public class FlywayConfig {

    /**
     * En desarrollo local, permite limpiar y re-crear el schema con:
     * mvn flyway:clean flyway:migrate
     *
     * Solo activo en perfil "local" — NUNCA en producción.
     */
    @Bean
    @Profile("local")
    public FlywayMigrationStrategy localCleanMigrationStrategy() {
        return flyway -> {
            // Descomenta esta línea solo cuando necesites resetear la BD local:
            // flyway.clean();
            flyway.migrate();
        };
    }
}
