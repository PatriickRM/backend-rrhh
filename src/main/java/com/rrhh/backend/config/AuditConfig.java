package com.rrhh.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Activa Spring Data JPA Auditing.
 *
 * AuditorAware resuelve el username del usuario autenticado desde el SecurityContext
 * de Spring Security para poblarlo en @CreatedBy y @LastModifiedBy.
 *
 * Si no hay usuario autenticado (ej: un scheduled job o seed de datos),
 * devuelve "system" como valor por defecto.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("system");
            }

            return Optional.of(authentication.getName());
        };
    }
}
