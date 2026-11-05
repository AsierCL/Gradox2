package com.example.gradox2;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Fija el contrato OpenAPI que consume el frontend y detecta cuándo se desincroniza
 * el archivo versionado.
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OpenApiContractTest {

    private static final List<String> PUBLIC_OPERATIONS = List.of(
            "post /api/auth/login",
            "post /api/auth/register",
            "post /api/auth/logout",
            "post /api/auth/token/refresh",
            "post /api/auth/password/reset",
            "post /api/auth/password/reset-request",
            "get /api/auth/verify",
            "get /files/all",
            "get /files/{id}",
            "get /files/{id}/download",
            "get /files/{id}/comments",
            "get /subjects",
            "get /subjects/{id}",
            "get /courses",
            "get /courses/{id}",
            "get /badges",
            "get /stats",
            "get /health");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private JsonNode fetchSpec() throws Exception {
        String body = restTemplate.getForObject("/v3/api-docs", String.class);
        JsonNode spec = objectMapper.readTree(body);
        // El puerto es aleatorio: `servers` cambia en cada ejecución y no forma
        // parte del contrato que versionamos.
        if (spec.isObject()) {
            ((com.fasterxml.jackson.databind.node.ObjectNode) spec).remove("servers");
        }
        return spec;
    }

    @Test
    void publicOperationsDoNotRequireABearerToken() throws Exception {
        JsonNode paths = fetchSpec().path("paths");

        for (String operation : PUBLIC_OPERATIONS) {
            String[] parts = operation.split(" ", 2);
            JsonNode node = paths.path(parts[1]).path(parts[0]).path("security");
            assertThat(node.isArray())
                    .as("%s debe declarar security: [] (endpoint abierto a GUEST)", operation)
                    .isTrue();
            assertThat(node).isEmpty();
        }
    }

    @Test
    void protectedOperationsDocument401AndErrorsUseTheErrorSchema() throws Exception {
        JsonNode paths = fetchSpec().path("paths");
        JsonNode errorSchema = fetchSpec().path("components").path("schemas").path("ErrorDTO");
        assertThat(errorSchema.isMissingNode()).as("el schema ErrorDTO debe existir").isFalse();

        JsonNode me = paths.path("/users/me").path("get");
        assertThat(me.path("security").isArray()).as("/users/me requiere token").isFalse();
        assertThat(me.path("responses").has("401")).as("/users/me documenta 401").isTrue();

        for (Map.Entry<String, JsonNode> path : iterable(paths.fields())) {
            for (Map.Entry<String, JsonNode> op : iterable(path.getValue().fields())) {
                JsonNode responses = op.getValue().path("responses");
                for (Map.Entry<String, JsonNode> response : iterable(responses.fields())) {
                    if (!response.getKey().startsWith("4") && !response.getKey().startsWith("5")) {
                        continue;
                    }
                    String ref = response.getValue().path("content").path("application/json")
                            .path("schema").path("$ref").asText("");
                    assertThat(ref)
                            .as("%s %s -> %s debe referenciar el schema de error",
                                    op.getKey().toUpperCase(), path.getKey(), response.getKey())
                            .isEqualTo("#/components/schemas/ErrorDTO");
                }
            }
        }
    }

    @Test
    void tagListHasNoDuplicatesAndHealthIsGetOnly() throws Exception {
        JsonNode spec = fetchSpec();
        List<String> names = new ArrayList<>();
        spec.path("tags").forEach(tag -> names.add(tag.path("name").asText()));
        assertThat(names).doesNotHaveDuplicates();

        List<String> verbs = new ArrayList<>();
        spec.path("paths").path("/health").fieldNames().forEachRemaining(verbs::add);
        assertThat(verbs).containsExactly("get");
    }

    private static <T> Iterable<T> iterable(java.util.Iterator<T> iterator) {
        return () -> iterator;
    }
}
