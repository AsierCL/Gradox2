package com.example.gradox2.presentation.dto.badges;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Catálogo de una insignia")
public class BadgeResponse {
    @Schema(description = "ID de la insignia", example = "1")
    public Long id;

    @Schema(description = "Nombre de la insignia", example = "PRIMER_ARCHIVO")
    public String name;

    @Schema(description = "Descripción de la insignia", example = "Sube tu primer archivo")
    public String description;

    @Schema(description = "URL firmada del icono (null si el icono no está configurado)")
    public String iconUrl;
}