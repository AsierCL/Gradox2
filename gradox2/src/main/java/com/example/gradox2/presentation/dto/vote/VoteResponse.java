package com.example.gradox2.presentation.dto.vote;

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
@Schema(description = "Respuesta con los datos de un voto")
public class VoteResponse {
    @Schema(description = "ID del voto", example = "1")
    public Long idVote;

    @Schema(description = "ID de la propuesta votada", example = "1")
    public Long proposalId;

    @Schema(description = "Nombre del votante (puede ser 'anonymous' según visibilidad)", example = "aliceuser")
    public String voter;

    @Schema(description = "true = a favor, false = en contra", example = "true")
    public Boolean inFavor;

    @Schema(description = "Momento del voto")
    public Instant votedAt;
}
