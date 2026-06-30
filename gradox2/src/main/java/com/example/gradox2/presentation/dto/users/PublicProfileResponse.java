package com.example.gradox2.presentation.dto.users;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Perfil público de un usuario")
public class PublicProfileResponse {
    @Schema(description = "Nombre de usuario", example = "aliceuser")
    public String username;

    @Schema(description = "Rol (USER, MASTER, GUEST)", example = "USER")
    public String role;

    @Schema(description = "Puntuación de reputación", example = "4.5")
    public Double reputation;

    @Schema(description = "Insignias obtenidas", example = "[\"PRIMER_ARCHIVO\"]")
    public Set<String> badges;
}
