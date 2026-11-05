package com.example.gradox2.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;

import com.example.gradox2.presentation.dto.auth.ErrorDTO;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        var securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Gradox2 API")
                        .version("0.1")
                        .description("API de gestión de archivos académicos para la aplicación Gradox2")
                        .contact(new Contact()
                                .name("Gradox2 Team")
                                .email("gradox2app@gmail.com"))
                        .license(new License()
                                .name("GPL-3.0")
                                .url("https://www.gnu.org/licenses/gpl-3.0.html")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    /**
     * Ordena los tags de Swagger UI siguiendo la lista configurada en
     * {@code springdoc.tags-order}. Los tags no incluidos en la lista se colocan
     * al final, en orden alfabético. Así se puede insertar una categoría en
     * cualquier posición sin renumerar nada.
     */
    @Bean
    public GlobalOpenApiCustomizer tagsOrderCustomizer(
            @Value("${springdoc.tags-order:}") String tagsOrder) {
        return openApi -> {
            List<Tag> byName = new ArrayList<>(openApi.getTags());

            List<String> orderedNames = List.of(tagsOrder.split(",", -1));
            List<Tag> ordered = new ArrayList<>();
            List<String> unmatched = new ArrayList<>(orderedNames.stream().map(String::trim).toList());

            for (String name : unmatched) {
                for (int i = 0; i < byName.size(); i++) {
                    if (name.equalsIgnoreCase(byName.get(i).getName())) {
                        ordered.add(byName.remove(i));
                        break;
                    }
                }
            }

            byName.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
            ordered.addAll(byName);
            openApi.setTags(ordered);
        };
    }

    /**
     * Operaciones que la API expone sin cuenta, tal como las habilita
     * {@code SecurityConfig} (catálogo, archivos visibles para invitados y todo
     * {@code /api/auth/**}). Se marcan con {@code security: []} para que un
     * cliente generado no exija token en ellas.
     */
    private static final Set<String> PUBLIC_OPERATIONS = Set.of(
            "POST /api/auth/login",
            "POST /api/auth/register",
            "POST /api/auth/logout",
            "POST /api/auth/token/refresh",
            "POST /api/auth/password/reset",
            "POST /api/auth/password/reset-request",
            "GET /api/auth/verify",
            "GET /files/all",
            "GET /files/{id}",
            "GET /files/{id}/download",
            "GET /files/{id}/comments",
            "GET /subjects",
            "GET /subjects/{id}",
            "GET /courses",
            "GET /courses/{id}",
            "GET /badges",
            "GET /stats",
            "GET /health");

    private static final String ERROR_SCHEMA = "ErrorDTO";

    /**
     * Ajustes transversales del contrato que consume el frontend:
     * <ul>
     *   <li>elimina tags duplicados en la lista global;</li>
     *   <li>marca las operaciones públicas con {@code security: []};</li>
     *   <li>documenta {@code 401} en las operaciones que requieren cuenta;</li>
     *   <li>referencia el schema de error en todas las respuestas 4xx/5xx.</li>
     * </ul>
     */
    @Bean
    public GlobalOpenApiCustomizer contractCustomizer() {
        return openApi -> {
            if (openApi.getTags() != null) {
                Map<String, Tag> unique = new LinkedHashMap<>();
                for (Tag tag : openApi.getTags()) {
                    unique.putIfAbsent(tag.getName().toLowerCase(), tag);
                }
                openApi.setTags(new ArrayList<>(unique.values()));
            }

            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }
            ModelConverters.getInstance().read(ErrorDTO.class)
                    .forEach(openApi.getComponents()::addSchemas);

            if (openApi.getPaths() == null) {
                return;
            }

            for (Map.Entry<String, PathItem> path : openApi.getPaths().entrySet()) {
                for (Map.Entry<PathItem.HttpMethod, Operation> entry : path.getValue().readOperationsMap().entrySet()) {
                    Operation operation = entry.getValue();
                    String key = entry.getKey().name() + " " + path.getKey();

                    if (PUBLIC_OPERATIONS.contains(key)) {
                        operation.setSecurity(List.of());
                    } else {
                        operation.getResponses().computeIfAbsent("401",
                                code -> new ApiResponse().description("No autenticado: falta el token o es inválido"));
                    }

                    operation.getResponses().forEach((code, response) -> {
                        if (!code.startsWith("4") && !code.startsWith("5")) {
                            return;
                        }
                        Schema<?> errorRef = new Schema<>().$ref("#/components/schemas/" + ERROR_SCHEMA);
                        response.setContent(new Content()
                                .addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                        new MediaType().schema(errorRef)));
                    });
                }
            }
        };
    }
}
