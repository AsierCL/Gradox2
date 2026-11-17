package com.example.gradox2.presentation.dto.fileProposal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "Respuesta con los datos de una propuesta de archivo")
public class FileProposalResponse {
    @Schema(description = "ID de la propuesta", example = "1")
    private Long id;

    @Schema(description = "Título del archivo propuesto", example = "Apuntes Tema 1")
    private String title;

    @Schema(description = "Descripción del archivo", example = "Apuntes completos del tema 1")
    private String description;

    @Schema(description = "Nombre del proponente (según visibilidad puede ser 'anonymous')", example = "aliceuser")
    private String proposer;

    @Schema(description = "Nivel de visibilidad solicitado (PUBLIC, RESTRICTED, PRIVATE)", example = "PUBLIC")
    private String visibilityLevel;

    // El nombre de la asignatura no es único entre grados: los identificadores son obligatorios (§2.2).
    @Schema(description = "ID de la asignatura", example = "12")
    private Long subjectId;

    @Schema(description = "Código oficial de la asignatura", example = "G4012102")
    private String subjectCode;

    @Schema(description = "Nombre de la asignatura", example = "Álgebra")
    private String subjectName;

    @Schema(description = "ID del curso", example = "1")
    private Long courseId;

    @Schema(description = "Nombre del curso", example = "Primer curso")
    private String courseName;

    @Schema(description = "ID del grado", example = "1")
    private Long degreeId;

    @Schema(description = "Código oficial del grado", example = "G4012")
    private String degreeCode;

    @Schema(description = "Nombre del grado", example = "Ingeniería Informática")
    private String degreeName;

    @Schema(description = "Tipo de archivo (APUNTES, EJERCICIO, EXAMEN)", example = "APUNTES")
    private String type;

    @Schema(description = "Estado (PENDING, APPROVED, REJECTED)", example = "PENDING")
    private String status;
}
