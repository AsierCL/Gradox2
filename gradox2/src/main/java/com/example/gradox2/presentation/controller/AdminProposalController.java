package com.example.gradox2.presentation.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.example.gradox2.presentation.dto.admin.AdminProposalResponse;
import com.example.gradox2.service.interfaces.IAdminProposalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/admin/proposals")
@Validated
@Tag(name = "Administración de propuestas", description = "Acceso MASTER en solo lectura a todas las propuestas")
public class AdminProposalController {

    private final IAdminProposalService adminProposalService;

    public AdminProposalController(IAdminProposalService adminProposalService) {
        this.adminProposalService = adminProposalService;
    }

    @GetMapping
    @PreAuthorize("hasRole('MASTER')")
    @Operation(summary = "Listar todas las propuestas", description = "Devuelve todas las propuestas (subidas, borrados, promociones, cambios de política) paginadas (solo MASTER)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado de propuestas"),
        @ApiResponse(responseCode = "403", description = "No autorizado (se requiere MASTER)", content = @Content)
    })
    public ResponseEntity<Page<AdminProposalResponse>> getAllProposals(
            @Parameter(description = "Número de página (base 0)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página (máx 100)") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(adminProposalService.getAllProposals(page, size));
    }
}