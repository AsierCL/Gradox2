package com.example.gradox2.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.example.gradox2.presentation.dto.vote.VoteResponse;
import com.example.gradox2.presentation.dto.vote.VoteResultResponse;
import com.example.gradox2.service.interfaces.IVoteService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.Positive;



@RestController
@RequestMapping("/vote")
@Validated
@Tag(name = "06. Votaciones", description = "Votación sobre propuestas de subida/borrado y promociones")
public class VoteController {

    private final IVoteService voteService;

    public VoteController(IVoteService voteService) {
        this.voteService = voteService;
    }


    @GetMapping("/{id}")
    @Operation(summary = "Mi voto", description = "Obtiene el voto del usuario autenticado en una propuesta")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Voto devuelto"),
        @ApiResponse(responseCode = "404", description = "Propuesta o voto no encontrado", content = @Content)
    })
    public ResponseEntity<VoteResponse> getMyVoteProposal(
            @Parameter(description = "ID de la propuesta") @PathVariable @Positive Long id) {
        return ResponseEntity.ok(voteService.getMyVote(id));
    }


    @GetMapping("/{id}/results")
    @Operation(summary = "Resultados", description = "Obtiene el recuento actual de votos de una propuesta")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resultados devueltos"),
        @ApiResponse(responseCode = "404", description = "Propuesta no encontrada", content = @Content)
    })
    public ResponseEntity<VoteResultResponse> getAllVotesProposal(
            @Parameter(description = "ID de la propuesta") @PathVariable @Positive Long id) {
        return ResponseEntity.ok(voteService.getVoteCount(id));
    }


    @PostMapping("/{id}/{upvote}")
    @Operation(summary = "Votar propuesta", description = "Vota a favor (true) o en contra (false) de una propuesta")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Voto registrado"),
        @ApiResponse(responseCode = "400", description = "Propuesta cerrada o ya votaste", content = @Content),
        @ApiResponse(responseCode = "404", description = "Propuesta no encontrada", content = @Content)
    })
    public ResponseEntity<VoteResponse> voteProposal(
            @Parameter(description = "ID de la propuesta") @PathVariable @Positive Long id,
            @Parameter(description = "true = a favor, false = en contra") @PathVariable boolean upvote) {
        return ResponseEntity.ok(voteService.voteProposal(id, upvote));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Retirar voto", description = "Elimina el voto del usuario autenticado en una propuesta")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Voto retirado"),
        @ApiResponse(responseCode = "404", description = "Propuesta o voto no encontrado", content = @Content)
    })
    public ResponseEntity<String> retractVote(
            @Parameter(description = "ID de la propuesta") @PathVariable @Positive Long id) {
        return ResponseEntity.ok(voteService.retractVote(id));
    }


}
