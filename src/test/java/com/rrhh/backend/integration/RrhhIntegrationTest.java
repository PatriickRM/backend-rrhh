package com.rrhh.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrhh.backend.domain.model.*;
import com.rrhh.backend.domain.repository.*;
import com.rrhh.backend.web.dto.login.LoginRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración end-to-end.
 * Levanta MySQL real con Testcontainers — sin mocks, sin H2.
 *
 * Requiere Docker corriendo en la máquina que ejecuta los tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tests de integración — flujo completo RRHH")
class RrhhIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("rrhh_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private PositionRepository positionRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static String chroToken;
    private static String employeeToken;
    private static Long departmentId;
    private static Long positionId;

    @BeforeEach
    void setUp() {
        // Crear roles si no existen
        if (!roleRepository.existsByName("CHRO")) {
            roleRepository.save(new Role(null, "CHRO"));
        }
        if (!roleRepository.existsByName("EMPLOYEE")) {
            roleRepository.save(new Role(null, "EMPLOYEE"));
        }
        if (!roleRepository.existsByName("HEAD")) {
            roleRepository.save(new Role(null, "HEAD"));
        }
    }

    // ─────────────────────────────────────────────
    // Auth
    // ─────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("1. Debe poder autenticarse como CHRO y recibir token JWT")
    void autenticacion_chro_retorna_token() throws Exception {
        Role chroRole = roleRepository.findByName("CHRO").orElseThrow();

        User chroUser = User.builder()
                .username("chro_admin")
                .fullName("Director RRHH")
                .password(passwordEncoder.encode("Admin1234!"))
                .enabled(true)
                .roles(Set.of(chroRole))
                .build();
        userRepository.save(chroUser);

        LoginRequest loginRequest = LoginRequest.builder()
                .username("chro_admin")
                .password("Admin1234!")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("chro_admin"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        chroToken = objectMapper.readTree(responseBody).get("token").asText();
        assertThat(chroToken).isNotBlank();
    }

    @Test
    @Order(2)
    @DisplayName("2. Debe rechazar login con credenciales incorrectas")
    void login_con_credenciales_invalidas_retorna_400() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("no_existe")
                .password("clave_incorrecta")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    // ─────────────────────────────────────────────
    // Departments
    // ─────────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("3. CHRO debe poder crear un departamento")
    void chro_puede_crear_departamento() throws Exception {
        if (chroToken == null) autenticacion_chro_retorna_token();

        String body = """
                {
                    "code": "TI-001",
                    "name": "Tecnología de la Información",
                    "description": "Departamento de sistemas e infraestructura"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/departments")
                        .header("Authorization", "Bearer " + chroToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Tecnología de la Información"))
                .andExpect(jsonPath("$.code").value("TI-001"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        departmentId = objectMapper.readTree(responseBody).get("id").asLong();
        assertThat(departmentId).isPositive();
    }

    @Test
    @Order(4)
    @DisplayName("4. No debe permitir crear departamento con código duplicado")
    void no_permite_departamento_con_codigo_duplicado() throws Exception {
        if (chroToken == null) autenticacion_chro_retorna_token();
        if (departmentId == null) chro_puede_crear_departamento();

        String body = """
                {
                    "code": "TI-001",
                    "name": "Otro Departamento",
                    "description": "Duplicado intencional"
                }
                """;

        mockMvc.perform(post("/api/departments")
                        .header("Authorization", "Bearer " + chroToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    // ─────────────────────────────────────────────
    // Positions
    // ─────────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("5. CHRO debe poder crear una posición en el departamento")
    void chro_puede_crear_posicion() throws Exception {
        if (chroToken == null) autenticacion_chro_retorna_token();
        if (departmentId == null) chro_puede_crear_departamento();

        String body = String.format("""
                {
                    "title": "Desarrollador Senior",
                    "baseSalary": 4500.00,
                    "departmentId": %d
                }
                """, departmentId);

        MvcResult result = mockMvc.perform(post("/api/positions")
                        .header("Authorization", "Bearer " + chroToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Desarrollador Senior"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        positionId = objectMapper.readTree(responseBody).get("id").asLong();
        assertThat(positionId).isPositive();
    }

    @Test
    @Order(6)
    @DisplayName("6. No debe permitir salario menor al mínimo legal (1200)")
    void no_permite_salario_menor_al_minimo_legal() throws Exception {
        if (chroToken == null) autenticacion_chro_retorna_token();
        if (departmentId == null) chro_puede_crear_departamento();

        String body = String.format("""
                {
                    "title": "Asistente",
                    "baseSalary": 800.00,
                    "departmentId": %d
                }
                """, departmentId);

        mockMvc.perform(post("/api/positions")
                        .header("Authorization", "Bearer " + chroToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("1200")));
    }

    // ─────────────────────────────────────────────
    // Seguridad
    // ─────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("7. Endpoints protegidos deben retornar 401 sin token")
    void endpoints_protegidos_requieren_autenticacion() throws Exception {
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    @DisplayName("8. CHRO puede listar todos los departamentos")
    void chro_puede_listar_departamentos() throws Exception {
        if (chroToken == null) autenticacion_chro_retorna_token();

        mockMvc.perform(get("/api/departments")
                        .header("Authorization", "Bearer " + chroToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
