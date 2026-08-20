package com.example.gradox2.presentation.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "Asignatura resumida dentro de un curso")
public class CourseSubjectResponse {
    @Schema(description = "ID de la asignatura", example = "1")
    private Long id;

    @Schema(description = "Código de la asignatura", example = "FMAT1")
    private String code;

    @Schema(description = "Nombre de la asignatura", example = "Fundamentos de Matemáticas")
    private String name;
}