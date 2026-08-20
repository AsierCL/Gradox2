package com.example.gradox2.presentation.dto.course;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "Respuesta con los datos de un curso y sus asignaturas")
public class CourseResponse {
    @Schema(description = "ID del curso", example = "1")
    private Long id;

    @Schema(description = "Código del curso", example = "1")
    private String code;

    @Schema(description = "Nombre del curso", example = "Primer curso")
    private String name;

    @Schema(description = "Asignaturas del curso")
    private List<CourseSubjectResponse> subjects;
}