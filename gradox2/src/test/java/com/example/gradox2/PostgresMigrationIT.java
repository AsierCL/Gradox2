package com.example.gradox2;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
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

    /**
     * Ensaya la migración multi-grado (V10) sobre una base que ya está en V9 con datos: comprueba que el
     * backfill asigna Informática a todo lo existente, que el código de curso deja de ser único global y,
     * sobre todo, que los archivos siguen apuntando a la misma asignatura (el renombrado de códigos llega
     * en la V11 y no puede re-apuntar `files.subject_id`).
     *
     * <p>Se ejecuta en un schema propio para no interferir con el schema que migra el contexto de Spring.
     */
    @Test
    void multiDegreeMigrationBackfillsExistingData() throws SQLException {
        String url = requireEnv("POSTGRES_TEST_URL");
        String user = envOrDefault("POSTGRES_TEST_USER", "postgres");
        String pass = envOrDefault("POSTGRES_TEST_PASSWORD", "postgres");
        String schema = "multi_degree_backfill";

        dropSchema(url, user, pass, schema);

        Flyway.configure()
                .dataSource(url, user, pass)
                .schemas(schema)
                .defaultSchema(schema)
                .target(MigrationVersion.fromVersion("9"))
                .load()
                .migrate();

        try (Connection connection = DriverManager.getConnection(url, user, pass);
                Statement statement = connection.createStatement()) {
            statement.execute("set search_path to " + schema);
            statement.executeUpdate("""
                    insert into users (enabled, reputation, created_at, email, password_hash, role, username)
                    values (true, 10.0, now(), 'legacy@rai.usc.es', 'hash', 'USER', 'legacyuser')
                    """);
            statement.executeUpdate("""
                    insert into files (subject_id, uploader_id, object_key, title, type, visibility_level)
                    select s.id, u.id, 'legacy/file.pdf', 'Apuntes previos', 'APUNTES', 'PUBLIC'
                      from subjects s, users u
                     where s.code = 'FMAT1' and u.username = 'legacyuser'
                    """);
            statement.executeUpdate("""
                    insert into temp_files (subject_id, uploader_id, object_key, title, type, visibility_level)
                    select s.id, u.id, 'legacy/temp.pdf', 'Propuesta previa', 'EXAMEN', 'RESTRICTED'
                      from subjects s, users u
                     where s.code = 'FMAT1' and u.username = 'legacyuser'
                    """);
        }

        long fileSubjectBefore = queryLong(url, user, pass, schema,
                "select subject_id from files order by id limit 1");
        long tempFileSubjectBefore = queryLong(url, user, pass, schema,
                "select subject_id from temp_files order by id limit 1");

        Flyway.configure()
                .dataSource(url, user, pass)
                .schemas(schema)
                .defaultSchema(schema)
                .load()
                .migrate();

        assertThat(queryLong(url, user, pass, schema, "select count(*) from degrees"))
                .as("deben existir los tres grados").isEqualTo(3);
        assertThat(queryLong(url, user, pass, schema, "select count(*) from users where degree_id is null"))
                .as("ningún usuario puede quedar sin grado").isZero();
        assertThat(queryString(url, user, pass, schema,
                "select d.code from users u join degrees d on d.id = u.degree_id where u.username = 'legacyuser'"))
                .as("los usuarios existentes pasan a Ingeniería Informática").isEqualTo("G4012");
        assertThat(queryLong(url, user, pass, schema,
                "select count(*) from courses c join degrees d on d.id = c.degree_id where d.code = 'G4012'"))
                .as("los cuatro cursos existentes pasan a Ingeniería Informática").isEqualTo(4);
        assertThat(queryLong(url, user, pass, schema, "select subject_id from files order by id limit 1"))
                .as("el archivo debe seguir apuntando a la misma asignatura").isEqualTo(fileSubjectBefore);
        assertThat(queryLong(url, user, pass, schema, "select subject_id from temp_files order by id limit 1"))
                .as("la propuesta debe seguir apuntando a la misma asignatura").isEqualTo(tempFileSubjectBefore);

        // ── catálogo completo (V11) ────────────────────────────────────────────
        assertThat(queryLong(url, user, pass, schema, "select count(*) from subjects"))
                .as("49 de Informática + 47 de Química + 50 de IA").isEqualTo(146);
        assertThat(queryLong(url, user, pass, schema,
                "select count(*) from subjects s join courses c on c.id = s.course_id "
                        + "join degrees d on d.id = c.degree_id where d.code = 'G4012'"))
                .as("asignaturas de Informática").isEqualTo(49);
        assertThat(queryLong(url, user, pass, schema,
                "select count(*) from subjects s join courses c on c.id = s.course_id "
                        + "join degrees d on d.id = c.degree_id where d.code = 'G4022'"))
                .as("asignaturas de Química").isEqualTo(47);
        assertThat(queryLong(url, user, pass, schema,
                "select count(*) from subjects s join courses c on c.id = s.course_id "
                        + "join degrees d on d.id = c.degree_id where d.code = 'G4121'"))
                .as("asignaturas de IA").isEqualTo(50);
        assertThat(queryLong(url, user, pass, schema, "select count(*) from courses"))
                .as("cuatro cursos por cada uno de los tres grados").isEqualTo(12);
        assertThat(queryString(url, user, pass, schema,
                "select code from subjects where id = " + fileSubjectBefore))
                .as("la asignatura del archivo se renombra en su misma fila, no se recrea")
                .isEqualTo("G4012101");
        assertThat(queryLong(url, user, pass, schema, "select count(*) from subjects where code = 'FMAT1'"))
                .as("el código antiguo ya no existe").isZero();
        assertThat(queryString(url, user, pass, schema,
                "select credits from subjects where code = 'G4012226'"))
                .as("los créditos con decimal del plan oficial se conservan").isEqualTo("4.5");
        assertThat(queryString(url, user, pass, schema,
                "select coalesce(semester, 'null') from subjects where code = 'G4121450'"))
                .as("una asignatura anual no tiene semestre").isEqualTo("null");

        assertThat(queryLong(url, user, pass, schema, "select count(*) from courses where code = '1'"))
                .as("el código de curso ya no es único global: '1' existe en los tres grados").isEqualTo(3);

        boolean duplicateRejected = false;
        try (Connection connection = DriverManager.getConnection(url, user, pass);
                Statement statement = connection.createStatement()) {
            statement.execute("set search_path to " + schema);
            statement.executeUpdate("""
                    insert into courses (code, name, degree_id)
                    select '1', 'Primer curso', id from degrees where code = 'G4012'
                    """);
        } catch (SQLException expected) {
            duplicateRejected = true;
        }
        assertThat(duplicateRejected)
                .as("el mismo código de curso sigue prohibido dentro del mismo grado").isTrue();

        // ── rol de plataforma (V12): el check de role sigue vigente y admite ADMIN ──
        try (Connection connection = DriverManager.getConnection(url, user, pass);
                Statement statement = connection.createStatement()) {
            statement.execute("set search_path to " + schema);
            statement.executeUpdate("update users set role = 'ADMIN' where username = 'legacyuser'");
            statement.executeUpdate("update users set role = 'MASTER' where username = 'legacyuser'");
        }

        boolean invalidRoleRejected = false;
        try (Connection connection = DriverManager.getConnection(url, user, pass);
                Statement statement = connection.createStatement()) {
            statement.execute("set search_path to " + schema);
            statement.executeUpdate("update users set role = 'SUPERUSER' where username = 'legacyuser'");
        } catch (SQLException expected) {
            invalidRoleRejected = true;
        }
        assertThat(invalidRoleRejected)
                .as("el check de role debe seguir rechazando valores fuera del enum").isTrue();

        dropSchema(url, user, pass, schema);
    }

    private static void dropSchema(String url, String user, String pass, String schema) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, pass);
                Statement statement = connection.createStatement()) {
            statement.execute("drop schema if exists " + schema + " cascade");
        }
    }

    private static long queryLong(String url, String user, String pass, String schema, String sql)
            throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, pass);
                Statement statement = connection.createStatement()) {
            statement.execute("set search_path to " + schema);
            try (ResultSet rs = statement.executeQuery(sql)) {
                assertThat(rs.next()).as("la consulta debe devolver una fila: %s", sql).isTrue();
                return rs.getLong(1);
            }
        }
    }

    private static String queryString(String url, String user, String pass, String schema, String sql)
            throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, pass);
                Statement statement = connection.createStatement()) {
            statement.execute("set search_path to " + schema);
            try (ResultSet rs = statement.executeQuery(sql)) {
                assertThat(rs.next()).as("la consulta debe devolver una fila: %s", sql).isTrue();
                return rs.getString(1);
            }
        }
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        assertThat(value).as("la variable %s debe estar definida", name).isNotBlank();
        return value;
    }

    private static String envOrDefault(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
