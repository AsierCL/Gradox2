package com.example.gradox2.presentation.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Confirmación de restablecimiento de contraseña con token")
public class PasswordResetConfirmRequest {
    @NotBlank(message = "Token is required")
    @Schema(description = "Token de restablecimiento recibido por email", example = "abc123token")
    private String token;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
    @Schema(description = "Nueva contraseña", example = "NewSecurePass1!")
    private String newPassword;
}
