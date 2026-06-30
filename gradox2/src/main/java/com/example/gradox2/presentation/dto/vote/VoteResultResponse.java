package com.example.gradox2.presentation.dto.vote;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Resultado del recuento de votos de una propuesta")
public class VoteResultResponse {
    @Schema(description = "ID de la propuesta", example = "1")
    private Long fileId;

    @Schema(description = "Votos a favor", example = "5")
    private Long upvotes;

    @Schema(description = "Votos en contra", example = "2")
    private Long downvotes;
}
