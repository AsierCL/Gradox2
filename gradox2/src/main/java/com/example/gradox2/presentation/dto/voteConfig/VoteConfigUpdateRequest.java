package com.example.gradox2.presentation.dto.voteConfig;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Solicitud de actualización de la configuración global de votación")
public class VoteConfigUpdateRequest {
    @NotNull(message = "Quorum is required")
    @Min(value = 1, message = "Quorum must be at least 1")
    @Max(value = 10000, message = "Quorum must be <= 10000")
    @Schema(description = "Número mínimo de votos para alcanzar quórum", example = "5")
    private Integer quorumRequired;

    @NotNull(message = "Approval threshold is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Approval threshold must be > 0")
    @DecimalMax(value = "1.0", inclusive = true, message = "Approval threshold must be <= 1")
    @Schema(description = "Umbral de aprobación (0.0 - 1.0)", example = "0.6")
    private Double approvalThreshold;

    @Min(value = 1, message = "Max pending uploads must be at least 1")
    @Schema(description = "Límite de subidas pendientes por usuario", example = "3")
    private Integer maxPendingUploads;
}
