# 🏢 Sistema RRHH — Backend

> API REST para gestión de recursos humanos: empleados, departamentos, posiciones y solicitudes de permisos/vacaciones con flujo de aprobación multinivel.

[![CI](https://github.com/TU_USUARIO/rrhh-backend/actions/workflows/ci.yml/badge.svg)](https://github.com/TU_USUARIO/rrhh-backend/actions)
[![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## Tabla de contenidos

- [Descripción del proyecto](#descripción-del-proyecto)
- [Stack tecnológico](#stack-tecnológico)
- [Arquitectura](#arquitectura)
- [Flujo de aprobación de permisos](#flujo-de-aprobación-de-permisos)
- [Cómo levantar el proyecto](#cómo-levantar-el-proyecto)
- [Variables de entorno](#variables-de-entorno)
- [Documentación de la API](#documentación-de-la-api)
- [Tests](#tests)
- [Estructura del proyecto](#estructura-del-proyecto)

---

## Descripción del proyecto

Sistema de RRHH desarrollado con **Spring Boot 3** y **Java 21** que permite:

- Registrar y gestionar empleados, departamentos y posiciones
- Sistema de roles: **CHRO** (administrador RRHH), **HEAD** (jefe de departamento), **EMPLOYEE**
- Solicitud de permisos y vacaciones con **validaciones de negocio complejas**
- Flujo de aprobación de dos niveles: Jefe → RRHH
- Cálculo automático de saldo de vacaciones según antigüedad
- Autenticación con **JWT** y autorización por roles en cada endpoint
- Carga y gestión de archivos de evidencia

---

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.5.4 |
| Seguridad | Spring Security + JWT (jjwt 0.11.5) |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | MySQL 8.0 |
| Documentación | SpringDoc OpenAPI (Swagger UI) |
| Validaciones | Jakarta Bean Validation |
| Tests unitarios | JUnit 5 + Mockito + AssertJ |
| Tests integración | Testcontainers (MySQL real) |
| Cobertura | JaCoCo (mínimo 60%) |
| Build | Maven 3.9 |
| Contenedores | Docker + Docker Compose |
| CI/CD | GitHub Actions |

---

## Arquitectura

```
src/main/java/com/rrhh/backend/
├── web/                        # Capa de presentación
│   ├── controller/             # REST Controllers
│   └── dto/                    # Request/Response DTOs
├── application/                # Capa de aplicación
│   ├── service/                # Interfaces de servicios
│   │   └── impl/               # Implementaciones
│   ├── mapper/                 # Mappers entidad ↔ DTO
│   │   └── impl/
│   ├── validator/              # Validaciones de negocio
│   └── exception/              # Manejo global de errores
├── domain/                     # Capa de dominio
│   ├── model/                  # Entidades JPA + Enums
│   └── repository/             # Repositorios Spring Data
└── security/                   # Configuración de seguridad
    ├── filter/                 # Filtro JWT
    ├── util/                   # JwtUtil
    └── custom/                 # Entry points personalizados
```

---

## Flujo de aprobación de permisos

```
Empleado solicita permiso
        │
        ▼
[PENDIENTE_JEFE] ──(Jefe rechaza)──► [RECHAZADO]
        │
   (Jefe aprueba)
        │
        ▼
[PENDIENTE_RRHH] ──(RRHH rechaza)──► [RECHAZADO]
        │
   (RRHH aprueba)
        │
        ▼
    [APROBADO]
```

**Reglas de negocio implementadas:**
- Vacaciones: mínimo 1 año de antigüedad, bloques de 15 o 30 días, 15 días de anticipación
- Cita médica: máximo 1 día hábil, 1 día de anticipación
- Matrimonio: máximo 3 días, solo 1 vez en la vida laboral
- Fallecimiento familiar: hasta 5 días después del evento
- Control de solapamiento de fechas
- Límites anuales por tipo de permiso

---

## Cómo levantar el proyecto

### Opción 1: Docker (recomendado)

```bash
# 1. Clonar el repositorio
git clone https://github.com/TU_USUARIO/rrhh-backend.git
cd rrhh-backend

# 2. Configurar variables de entorno
cp .env.example .env
# Editar .env con tus valores

# 3. Levantar todo (MySQL + App)
docker compose up -d

# 4. Verificar que está corriendo
curl http://localhost:8080/actuator/health
```

La API estará disponible en `http://localhost:8080`  
Swagger UI en `http://localhost:8080/swagger-ui.html`

**Con Adminer** (UI para ver la base de datos):
```bash
docker compose --profile tools up -d
# Adminer en http://localhost:8090
```

### Opción 2: Desarrollo local

**Prerrequisitos:** Java 21, Maven 3.9+, MySQL 8.0 corriendo localmente

```bash
# 1. Crear la base de datos
mysql -u root -p -e "CREATE DATABASE rrhh_system;"

# 2. Configurar variables de entorno (o editar application.yml para local)
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=tu_clave
export SECURITY_JWT_SECRET_KEY=una_clave_secreta_larga_de_minimo_32_caracteres

# 3. Ejecutar
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

---

## Variables de entorno

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `DB_NAME` | Nombre de la base de datos | `rrhh_system` |
| `DB_USERNAME` | Usuario MySQL | `rrhh_user` |
| `DB_PASSWORD` | Contraseña MySQL | — |
| `DB_ROOT_PASSWORD` | Contraseña root MySQL (solo Docker) | — |
| `JWT_SECRET` | Clave secreta JWT (mínimo 32 chars) | — |
| `JWT_EXPIRATION` | Expiración del token en ms | `86400000` (24h) |
| `DDL_AUTO` | Estrategia DDL de Hibernate | `update` |

---

## Documentación de la API

Swagger UI disponible en: `http://localhost:8080/swagger-ui.html`

### Endpoints principales

| Método | Endpoint | Rol requerido | Descripción |
|--------|----------|---------------|-------------|
| POST | `/api/auth/login` | Público | Autenticación y obtención de token JWT |
| GET | `/api/departments` | CHRO | Listar todos los departamentos |
| POST | `/api/departments` | CHRO | Crear nuevo departamento |
| GET | `/api/employees` | CHRO | Listar todos los empleados |
| POST | `/api/employees` | CHRO | Registrar nuevo empleado |
| GET | `/api/employee/dashboard` | EMPLOYEE, HEAD | Dashboard del empleado autenticado |
| POST | `/api/employee/leave-requests` | EMPLOYEE, HEAD | Solicitar permiso o vacaciones |
| GET | `/api/employee/leave-requests/balance` | EMPLOYEE, HEAD | Consultar saldo de vacaciones |
| GET | `/api/head/leave-requests/pending` | HEAD | Ver solicitudes pendientes del departamento |
| PUT | `/api/head/leave-requests/respond` | HEAD | Aprobar o rechazar solicitud |
| GET | `/api/chro/leave-requests/all` | CHRO | Ver todas las solicitudes del sistema |
| PUT | `/api/chro/leave-requests/respond` | CHRO | Decisión final de RRHH |

### Ejemplo de autenticación

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "Admin1234!"}'
```

Respuesta:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "fullName": "Administrador RRHH",
  "roles": ["ROLE_CHRO"],
  "expiredAt": 1234567890000
}
```

Usar el token en siguientes requests:
```bash
curl http://localhost:8080/api/departments \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

## Tests

```bash
# Tests unitarios
./mvnw test

# Tests de integración (requiere Docker para Testcontainers)
./mvnw failsafe:integration-test

# Todos los tests + reporte de cobertura
./mvnw verify

# Ver reporte de cobertura
open target/site/jacoco/index.html
```

**Cobertura mínima configurada: 60%** (verificada en CI)

La suite de tests incluye:
- Tests unitarios de servicios con Mockito (LeaveRequestService, EmployeeService, validadores)
- Tests de controllers con MockMvc
- Tests de integración end-to-end con MySQL real vía Testcontainers

---

## Estructura del proyecto

```
rrhh-backend/
├── .github/
│   └── workflows/
│       └── ci.yml              # Pipeline GitHub Actions
├── docker/
│   └── mysql/
│       └── init.sql            # Datos iniciales (roles, admin)
├── src/
│   ├── main/
│   │   ├── java/com/rrhh/backend/
│   │   └── resources/
│   │       └── application.yml # Configuración por perfiles
│   └── test/
│       └── java/com/rrhh/backend/
│           ├── application/service/    # Tests unitarios
│           ├── web/controller/         # Tests de controllers
│           └── integration/            # Tests de integración
├── .env.example                # Plantilla de variables de entorno
├── .gitignore
├── docker-compose.yml          # Orquestación de servicios
├── Dockerfile                  # Build multi-stage
└── pom.xml
```

---

## Licencia

MIT — ver [LICENSE](LICENSE) para detalles.
