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

    public AuthResponse(String token, String refreshToken, String username, String role) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
        this.role = role;
    }
}
