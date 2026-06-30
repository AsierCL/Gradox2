package com.example.gradox2.presentation.dto.files;

import com.example.gradox2.persistence.entities.enums.FileType;
import com.example.gradox2.persistence.entities.enums.FileVisibility;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "Respuesta con los datos de un archivo publicado")
public class FileResponse {
    @Schema(description = "ID del archivo", example = "1")
    private Long id;

    @Schema(description = "Nombre del archivo", example = "Apuntes Tema 1")
    private String fileName;

    @Schema(description = "Descripción del archivo", example = "Apuntes completos del tema 1")
    private String description;

    @Schema(description = "Tipo de archivo (APUNTES, EJERCICIO, EXAMEN)")
    private FileType fileType;

    @Schema(description = "Nombre de la asignatura", example = "Matemáticas")
    private String subject;

    @Schema(description = "Nombre del uploader (según visibilidad puede ser 'anonymous')", example = "aliceuser")
    private String uploaderUsername;

    @Schema(description = "Nivel de visibilidad (PUBLIC, RESTRICTED, PRIVATE)", example = "PUBLIC")
    private FileVisibility visibilityLevel;

    @Schema(description = "Puntuación media (0.0 - 10.0)", example = "8.5")
    private Double score;
}
