package com.example.gradox2.presentation.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Solicitud con token JWT (refresh o verificación)")
public class TokenRequest {
    @NotBlank(message = "Token is required")
    @Schema(description = "Token JWT", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
}
