package com.example.gradox2.presentation.dto.admin;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Usuario baneado (deshabilitado)")
public class BannedUserResponse {
    @Schema(description = "ID del usuario", example = "1")
    public Long id;

    @Schema(description = "Nombre de usuario", example = "aliceuser")
    public String username;

    @Schema(description = "Email", example = "alice@example.com")
    public String email;

    @Schema(description = "Fecha de creación de la cuenta", example = "2025-01-01T10:00:00Z")
    public Instant createdAt;

    @Schema(description = "Último inicio de sesión", example = "2025-01-01T10:00:00Z")
    public Instant lastLogin;
}