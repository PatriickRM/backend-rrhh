package com.rrhh.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI rrhhOpenAPI() {
        return new OpenAPI()
                .info(buildInfo())
                .servers(buildServers())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, buildSecurityScheme()));
    }

    private Info buildInfo() {
        return new Info()
                .title("Sistema RRHH — API REST")
                .version("1.0.0")
                .description("""
                        API para gestión de recursos humanos:
                        empleados, departamentos, posiciones y solicitudes de permisos/vacaciones.
                        
                        **Roles del sistema:**
                        - `CHRO` — Acceso total: empleados, departamentos, posiciones, todas las solicitudes
                        - `HEAD` — Jefe de departamento: ver y responder solicitudes de su equipo
                        - `EMPLOYEE` — Empleado: crear solicitudes y ver su propio dashboard
                        
                        **Flujo de autenticación:**
                        1. `POST /api/auth/login` con username y password
                        2. Copiar el `token` de la respuesta
                        3. Hacer clic en el botón **Authorize** (🔒) arriba a la derecha
                        4. Ingresar `Bearer {token}` y confirmar
                        """)
                .contact(new Contact()
                        .name("Patrick Reyes")
                        .email("patriickrs2012@gmail.com")
                        .url("https://github.com/PatriickRM"))
                .license(new License()
                        .name("MIT")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private List<Server> buildServers() {
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Servidor local de desarrollo");

        Server dockerServer = new Server()
                .url("http://localhost:8080")
                .description("Servidor Docker (docker-compose up)");

        return List.of(localServer, dockerServer);
    }

    private SecurityScheme buildSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Token JWT obtenido desde POST /api/auth/login. Formato: Bearer {token}");
    }
}
