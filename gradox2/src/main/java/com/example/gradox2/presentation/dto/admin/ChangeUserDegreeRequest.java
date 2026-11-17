package com.example.gradox2.presentation.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Override administrativo del grado de un usuario (GRADOS.md §4.3).
 *
 * <p>Existe solo para corregir un error del registro: el grado es permanente por diseño (D2) y ningún
 * usuario puede cambiarlo por su cuenta. El motivo es obligatorio porque el cambio altera permisos de
 * forma retroactiva y queda en la auditoría.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Cambio administrativo del grado de un usuario")
public class ChangeUserDegreeRequest {

    @NotNull(message = "El grado es obligatorio")
    @Schema(description = "ID del grado nuevo", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long degreeId;

    @NotBlank(message = "El motivo es obligatorio")
    @Schema(description = "Motivo del cambio (queda en la auditoría)", example = "Error al registrarse",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;
}
