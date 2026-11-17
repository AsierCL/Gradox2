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

    // El nombre de la asignatura NO es único entre grados (hay dos "Álgebra"): el cliente debe agrupar
    // por subjectId y nunca por subject (§2.2).
    @Schema(description = "ID de la asignatura", example = "12")
    private Long subjectId;

    @Schema(description = "Código oficial de la asignatura", example = "G4012102")
    private String subjectCode;

    @Schema(description = "ID del grado del archivo", example = "1")
    private Long degreeId;

    @Schema(description = "Código oficial del grado", example = "G4012")
    private String degreeCode;

    @Schema(description = "Nombre del grado", example = "Ingeniería Informática")
    private String degreeName;

    @Schema(description = "Nombre del uploader (según visibilidad puede ser 'anonymous')", example = "aliceuser")
    private String uploaderUsername;

    @Schema(description = "Nivel de visibilidad (PUBLIC, RESTRICTED, PRIVATE)", example = "PUBLIC")
    private FileVisibility visibilityLevel;

    @Schema(description = "Puntuación media (0.0 - 10.0)", example = "8.5")
    private Double score;

    @Schema(description = "Número de valoraciones del archivo", example = "12")
    private Integer voteCount;

    @Schema(description = "Hash SHA-256 del contenido del archivo, en hexadecimal",
            example = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
    private String fileHash;
}
