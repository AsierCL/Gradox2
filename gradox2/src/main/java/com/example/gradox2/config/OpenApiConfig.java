package com.example.gradox2.config;

import java.util.ArrayList;
import java.util.List;

import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;

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
}
