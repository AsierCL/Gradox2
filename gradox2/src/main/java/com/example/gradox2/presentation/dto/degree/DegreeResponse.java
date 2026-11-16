package com.example.gradox2.presentation.dto.degree;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "Grado universitario")
public class DegreeResponse {

    @Schema(description = "ID del grado", example = "1")
    private Long id;

    @Schema(description = "Código oficial de la USC", example = "G4012")
    private String code;

    @Schema(description = "Nombre del grado", example = "Ingeniería Informática")
    private String name;

    @Schema(description = "Cantidad de cursos del grado", example = "4")
    private int coursesCount;
}
