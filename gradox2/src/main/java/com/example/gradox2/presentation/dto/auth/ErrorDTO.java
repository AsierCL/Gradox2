package com.example.gradox2.presentation.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Respuesta de error estándar")
public class ErrorDTO {
    @Schema(description = "Mensaje de error descriptivo", example = "Invalid credentials")
    private String errorMessage;

    @Schema(description = "Código de error para el cliente", example = "UNAUTHENTICATED_ACCESS")
    private String errorCode;
}
