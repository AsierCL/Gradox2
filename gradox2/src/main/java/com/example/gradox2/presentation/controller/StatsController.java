package com.example.gradox2.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.presentation.dto.stats.PlatformStatsResponse;
import com.example.gradox2.service.interfaces.IStatsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/stats")
@Tag(name = "Estadísticas", description = "Métricas colectivas de la plataforma para la comunidad")
public class StatsController {

    private final IStatsService statsService;

    public StatsController(IStatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    @Operation(summary = "Estadísticas de la plataforma", description = "Métricas globales: total de archivos, usuarios, almacenamiento y descargas, con desglose por tipo y asignatura")
    @ApiResponse(responseCode = "200", description = "Estadísticas devueltas")
    public ResponseEntity<PlatformStatsResponse> getPlatformStats() {
        return ResponseEntity.ok(statsService.getPlatformStats());
    }
}