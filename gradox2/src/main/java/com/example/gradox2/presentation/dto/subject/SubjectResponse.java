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
}
