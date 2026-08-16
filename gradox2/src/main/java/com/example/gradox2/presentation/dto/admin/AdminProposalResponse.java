package com.example.gradox2.presentation.dto.admin;

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
@Schema(description = "Propuesta visible por MASTER (todos los tipos, solo lectura)")
public class AdminProposalResponse {
    @Schema(description = "ID de la propuesta", example = "1")
    private Long id;

    @Schema(description = "Nombre del proponente", example = "aliceuser")
    private String proposer;

    @Schema(description = "Tipo de acción (UPLOAD, DELETE, PROMOTION, EXPULSION, POLICY_CHANGE, VETO)", example = "UPLOAD")
    private String actionType;

    @Schema(description = "Estado (PENDING, APPROVED, REJECTED)", example = "PENDING")
    private String status;

    @Schema(description = "Quórum requerido", example = "3")
    private int quorumRequired;

    @Schema(description = "Umbral de aprobación", example = "0.67")
    private double approvalThreshold;

    @Schema(description = "Fecha de creación")
    private Instant createdAt;

    @Schema(description = "Fecha de cierre de la votación")
    private Instant endsAt;

    @Schema(description = "Fecha de resolución (null si pendiente)")
    private Instant closedAt;

    @Schema(description = "Título del archivo (solo propuestas de archivo)")
    private String title;

    @Schema(description = "Nombre de la asignatura (solo propuestas de archivo)")
    private String subjectName;

    @Schema(description = "Nombre del candidato (solo propuestas de promoción/expulsión)")
    private String candidate;
}