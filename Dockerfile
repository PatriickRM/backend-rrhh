# ── Etapa 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copiar archivos de Maven primero (aprovecha cache de layers)
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Descargar dependencias (cacheadas si pom.xml no cambia)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copiar código fuente y compilar
COPY src/ src/
RUN ./mvnw package -DskipTests -q

# ── Etapa 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Crear usuario sin privilegios (buena práctica de seguridad)
RUN addgroup -S rrhh && adduser -S rrhh -G rrhh
USER rrhh

WORKDIR /app

# Copiar solo el JAR desde la etapa de build
COPY --from=builder /app/target/*.jar app.jar

# Puerto de la aplicación
EXPOSE 8080

# Health check básico
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Opciones de JVM optimizadas para contenedores
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
