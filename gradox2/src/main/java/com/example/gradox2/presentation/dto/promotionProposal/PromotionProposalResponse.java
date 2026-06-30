package com.example.gradox2.presentation.dto.promotionProposal;

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
@Schema(description = "Respuesta con datos de una propuesta de promoción/expulsión")
public class PromotionProposalResponse {
    @Schema(description = "ID de la propuesta", example = "1")
    private Long id;

    @Schema(description = "Nombre del proponente (puede ser 'anonymous')", example = "master1")
    private String proposer;

    @Schema(description = "Nombre del candidato", example = "aliceuser")
    private String candidate;

    @Schema(description = "Estado (PENDING, APPROVED, REJECTED)", example = "PENDING")
    private String status;

    @Schema(description = "Fecha de creación")
    private Instant createdAt;

    @Schema(description = "Fecha de cierre de la votación")
    private Instant endsAt;
}
