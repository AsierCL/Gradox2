package com.example.gradox2.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.presentation.dto.voteConfig.ConfigProposalResponse;
import com.example.gradox2.presentation.dto.voteConfig.VoteConfigUpdateRequest;
import com.example.gradox2.persistence.entities.GlobalConfig;
import com.example.gradox2.service.interfaces.IConfigProposalService;
import com.example.gradox2.service.interfaces.IGlobalConfigService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/vote-config")
@Tag(name = "Configuración de Votaciones", description = "Ajustes globales de quórum y umbral de aprobación (solo MASTER, vía propuesta aprobada)")
public class VoteConfigController {

    private final IGlobalConfigService voteConfigService;
    private final IConfigProposalService configProposalService;

    public VoteConfigController(IGlobalConfigService voteConfigService,
            IConfigProposalService configProposalService) {
        this.voteConfigService = voteConfigService;
        this.configProposalService = configProposalService;
    }

    @PutMapping
    @PreAuthorize("hasRole('MASTER')")
    @Operation(summary = "Proponer cambio de configuración", description = "Crea una propuesta de cambio de política que debe ser aprobada por el 80% de los MASTER (solo MASTER)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Propuesta de cambio creada"),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "No autorizado (se requiere MASTER)", content = @Content)
    })
    public ResponseEntity<ConfigProposalResponse> updateConfig(@Valid @RequestBody VoteConfigUpdateRequest request) {
        ConfigProposalResponse response = configProposalService.createConfigProposal(
                request.getQuorumRequired(),
                request.getApprovalThreshold(),
                request.getMaxPendingUploads(),
                request.getMasterVoteWeight(),
                request.getUserVoteWeight());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Ver configuración", description = "Obtiene la configuración actual del sistema de votación")
    @ApiResponse(responseCode = "200", description = "Configuración devuelta")
    public ResponseEntity<GlobalConfig> getConfig() {
        return ResponseEntity.ok(voteConfigService.getConfig());
    }
}