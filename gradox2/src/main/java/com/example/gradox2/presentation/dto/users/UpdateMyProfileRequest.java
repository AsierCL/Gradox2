package com.example.gradox2.presentation.dto.users;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Solicitud de actualización del perfil propio")
public class UpdateMyProfileRequest {
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Nuevo nombre de usuario", example = "nuevoalias")
    public String username;

    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
    @Schema(description = "Nueva contraseña", example = "NewSecurePass1!")
    public String password;
}
