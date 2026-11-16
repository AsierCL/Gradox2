package com.example.gradox2.presentation.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Schema(description = "Respuesta de autenticación con tokens JWT")
public class AuthResponse {
    @Schema(description = "Token JWT de acceso", example = "eyJhbGciOiJIUzI1NiJ9...")
    public String token;

    @Schema(description = "Token para renovar el acceso", example = "eyJhbGciOiJIUzI1NiJ9...")
    public String refreshToken;

    @Schema(description = "Nombre de usuario", example = "aliceuser")
    public String username;

    @Schema(description = "Rol del usuario (USER, MASTER, GUEST)", example = "USER")
    public String role;

    @Schema(description = "ID del grado del usuario", example = "1")
    public Long degreeId;

    @Schema(description = "Código oficial del grado", example = "G4012")
    public String degreeCode;

    @Schema(description = "Nombre del grado", example = "Ingeniería Informática")
    public String degreeName;

    /**
     * {@code true} solo para el rol de plataforma {@code ADMIN}. El frontend debe usar esto —y nunca
     * {@code role == "MASTER"}— para decidir si muestra el panel de administración (§9.3).
     */
    @Schema(description = "El usuario administra la plataforma (rol ADMIN)", example = "false")
    public Boolean isPlatformAdmin;

    public AuthResponse(String token, String refreshToken, String username, String role) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
        this.role = role;
    }
}
