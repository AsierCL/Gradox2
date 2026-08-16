package com.example.gradox2.presentation.dto.voteConfig;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "Propuesta de cambio de política de configuración")
public class ConfigProposalResponse {
    @Schema(description = "ID de la propuesta", example = "1")
    private Long id;

    @Schema(description = "Nombre del proponente", example = "master1")
    private String proposer;

    @Schema(description = "Estado (PENDING, APPROVED, REJECTED)", example = "PENDING")
    private String status;

    @Schema(description = "Quórum propuesto", example = "5")
    private Integer quorumRequired;

    @Schema(description = "Umbral de aprobación propuesto", example = "0.6")
    private Double approvalThreshold;

    @Schema(description = "Límite de subidas pendientes propuesto", example = "3")
    private Integer maxPendingUploads;

    @Schema(description = "Peso de voto MASTER propuesto", example = "2.0")
    private Double masterVoteWeight;

    @Schema(description = "Peso de voto de usuario propuesto", example = "1.0")
    private Double userVoteWeight;

    @Schema(description = "Fecha de creación")
    private Instant createdAt;

    @Schema(description = "Fecha de cierre de la votación")
    private Instant endsAt;
}