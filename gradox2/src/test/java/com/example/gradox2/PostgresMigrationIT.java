package com.example.gradox2;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Verifica que las migraciones Flyway se aplican sobre un PostgreSQL real y que
 * el mapeo JPA coincide con el schema resultante ({@code ddl-auto=validate}).
 *
 * <p>Esto no se puede cubrir con el perfil {@code test} normal: ahí la base es H2
 * con {@code create-drop} y Flyway desactivado, así que un desajuste entre
 * migraciones y entidades solo aparecería al arrancar producción.
 *
 * <p>El test solo corre cuando {@code POSTGRES_TEST_URL} está definido (el job de
 * CI levanta un service de PostgreSQL). La suite local (`./mvnw test`) no lo
 * incluye: el nombre termina en {@code IT}, que Surefire no matchea por defecto.
 */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "POSTGRES_TEST_URL", matches = ".+")
class PostgresMigrationIT {

    @Autowired
    private Flyway flyway;

    @DynamicPropertySource
    static void postgresDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getenv("POSTGRES_TEST_URL"));
        registry.add("spring.datasource.username", () -> envOrDefault("POSTGRES_TEST_USER", "postgres"));
        registry.add("spring.datasource.password", () -> envOrDefault("POSTGRES_TEST_PASSWORD", "postgres"));
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        // Flyway crea el schema; Hibernate solo valida que entidades y tablas coincidan.
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Test
    void migrationsApplyAndMatchEntityMapping() {
        assertThat(flyway.info().applied())
                .as("Flyway debe haber aplicado las migraciones")
                .isNotEmpty();
        assertThat(flyway.info().pending())
                .as("no deben quedar migraciones pendientes")
                .isEmpty();
        assertThat(flyway.info().current()).isNotNull();
        assertThat(flyway.info().current().getVersion()).isNotNull();
    }

    private static String envOrDefault(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
