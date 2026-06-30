package com.example.gradox2.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.presentation.dto.voteConfig.VoteConfigUpdateRequest;
import com.example.gradox2.persistence.entities.GlobalConfig;
import com.example.gradox2.service.interfaces.IGlobalConfigService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/vote-config")
@Tag(name = "07. Configuración de Votaciones", description = "Ajustes globales de quórum y umbral de aprobación (solo MASTER)")
public class VoteConfigController {

    private final IGlobalConfigService voteConfigService;

    public VoteConfigController(IGlobalConfigService voteConfigService) {
        this.voteConfigService = voteConfigService;
    }

    @PutMapping
    @PreAuthorize("hasRole('MASTER')")
    @Operation(summary = "Actualizar configuración", description = "Ajusta quórum, umbral de aprobación y límite de subidas pendientes (solo MASTER)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Configuración actualizada"),
        @ApiResponse(responseCode = "403", description = "No autorizado (se requiere MASTER)", content = @Content)
    })
        public ResponseEntity<GlobalConfig> updateConfig(@Valid @RequestBody VoteConfigUpdateRequest request) {
    return ResponseEntity.ok(
            voteConfigService.updateConfig(request.getQuorumRequired(), request.getApprovalThreshold(), request.getMaxPendingUploads())
    );
    }

    @GetMapping
    @Operation(summary = "Ver configuración", description = "Obtiene la configuración actual del sistema de votación")
    @ApiResponse(responseCode = "200", description = "Configuración devuelta")
    public ResponseEntity<GlobalConfig> getConfig() {
        return ResponseEntity.ok(voteConfigService.getConfig());
    }
}
