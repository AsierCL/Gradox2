# Imagen base con JDK 21 (build stage)
FROM eclipse-temurin:21-jdk AS build

# Instalar Maven y compilar la app
WORKDIR /app
COPY gradox2/pom.xml .
COPY gradox2/mvnw .
COPY gradox2/.mvn .mvn
RUN ./mvnw dependency:go-offline

COPY gradox2/src src
RUN ./mvnw package -DskipTests

# Imagen final: JRE 21 (más ligera, solo runtime)
FROM eclipse-temurin:21-jre

# curl para el healthcheck HTTP (se instala antes de bajar a usuario no-root)
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Usuario no-root de aplicación (mejor práctica de seguridad)
RUN groupadd --system appuser \
    && useradd --system --gid appuser --shell /usr/sbin/nologin --home-dir /app appuser

WORKDIR /app
COPY --from=build /app/target/gradox2-0.0.1-SNAPSHOT.jar app.jar

# Directorio de logs (propietario appuser para que logback pueda rotar)
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

USER appuser

# Opciones JVM por defecto; sobreescribibles vía compose (JAVA_OPTS)
# MaxRAMPercentage respeta el límite de memoria del contenedor (cgroup v2)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport -XX:+ExitOnOutOfMemoryError"

# Puerto por defecto de Spring Boot
EXPOSE 8080

# Healthcheck vía Actuator: verifica app + conexión a PostgreSQL (componentes /actuator/health)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

# exec java mantiene a java como PID 1 (recibe señales de Docker para graceful shutdown)
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
