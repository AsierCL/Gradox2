package com.example.gradox2.presentation.dto.subject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "Respuesta con los datos de una asignatura")
public class SubjectResponse {
    @Schema(description = "ID de la asignatura", example = "1")
    private Long id;

    @Schema(description = "Código de la asignatura", example = "MAT1")
    private String code;

    @Schema(description = "Nombre de la asignatura", example = "Matemáticas I")
    private String name;

    @Schema(description = "ID del curso al que pertenece", example = "1")
    private Long courseId;

    @Schema(description = "Código del curso", example = "1")
    private String courseCode;

    @Schema(description = "Nombre del curso", example = "Primer curso")
    private String courseName;

    @Schema(description = "ID del grado al que pertenece la asignatura", example = "1")
    private Long degreeId;

    @Schema(description = "Código oficial del grado", example = "G4012")
    private String degreeCode;

    @Schema(description = "Nombre del grado", example = "Ingeniería Informática")
    private String degreeName;

    @Schema(description = "Créditos ECTS del plan oficial", example = "4.5")
    private Double credits;

    @Schema(description = "Semestre del plan oficial (nulo si es anual)", example = "Segundo semestre")
    private String semester;

    @Schema(description = "Tipo de materia del plan oficial", example = "Obligatorio")
    private String subjectType;
}
