package com.example.gradox2.presentation.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Solicitud de registro de nuevo usuario")
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@rai\\.usc\\.(es|gal)$",
        message = "El correo debe ser del dominio rai.usc.es o rai.usc.gal"
    )
    @Schema(description = "Correo institucional (@rai.usc.es o @rai.usc.gal)", example = "alice@rai.usc.es")
    public String email;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Nombre de usuario", example = "aliceuser")
    public String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
    @Schema(description = "Contraseña (mín 8 caracteres)", example = "SecurePass1!")
    public String password;

    /**
     * Grado que cursa el usuario. Obligatorio (D1) e inmutable después del alta (D2): define en qué
     * comunidad puede escribir. La validación de presencia vive en el servicio para devolver el código
     * estable {@code DEGREE_REQUIRED} en lugar de un error de validación genérico.
     */
    @Schema(description = "ID del grado elegido (obligatorio e inmutable)", example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    public Long degreeId;
}
