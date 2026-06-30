package com.example.gradox2.presentation.dto.users;

import java.time.Instant;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Perfil completo del usuario autenticado")
public class MyProfileResponse {
    @Schema(description = "ID del usuario", example = "1")
    public Long id;

    @Schema(description = "Nombre de usuario", example = "aliceuser")
    public String username;

    @Schema(description = "Correo electrónico", example = "alice@rai.usc.es")
    public String email;

    @Schema(description = "Rol (USER, MASTER, GUEST)", example = "USER")
    public String role;

    @Schema(description = "Puntuación de reputación", example = "4.5")
    public Double reputation;

    @Schema(description = "Fecha de creación de la cuenta")
    public Instant createdAt;

    @Schema(description = "Último inicio de sesión")
    public Instant lastLogin;

    @Schema(description = "Insignias obtenidas", example = "[\"PRIMER_ARCHIVO\", \"COLABORADOR\"]")
    public Set<String> badges;
}
