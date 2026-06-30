package com.example.gradox2.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.presentation.dto.promotionProposal.PromotionProposalResponse;
import com.example.gradox2.service.interfaces.IRoleService;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;


@RestController
@RequestMapping("/promoteProposal")
@Validated
@Tag(name = "03. Roles y Promociones", description = "Solicitudes de ascenso a MASTER y propuestas de expulsión")
public class RolesController {
    private final IRoleService roleService;

    public RolesController(IRoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/request")
    @Operation(summary = "Solicitar promoción", description = "Crea una solicitud de ascenso a MASTER para el usuario autenticado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Solicitud creada"),
        @ApiResponse(responseCode = "400", description = "Ya existe una solicitud pendiente", content = @Content)
    })
    public ResponseEntity<PromotionProposalResponse> promoteToMaster() {
        return ResponseEntity.ok(roleService.promoteToMaster());
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Cancelar solicitud", description = "Elimina la propia solicitud de promoción pendiente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Solicitud cancelada"),
        @ApiResponse(responseCode = "404", description = "No hay solicitud pendiente", content = @Content)
    })
    public ResponseEntity<String> deleteMyPromoteRequest() {
        return ResponseEntity.ok(roleService.deleteMyPromoteRequest());
    }

    @GetMapping("/pending")
    @Operation(summary = "Promociones pendientes", description = "Lista todas las solicitudes de promoción pendientes, con paginación opcional")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista devuelta"),
        @ApiResponse(responseCode = "403", description = "No autorizado (se requiere MASTER)", content = @Content)
    })
    public ResponseEntity<?> getPendingPromoteProposals(
            @Parameter(description = "Activar paginación") @RequestParam(defaultValue = "false") Boolean paged,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página (máx 100)") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @Parameter(description = "Campo de ordenación") @RequestParam(defaultValue = "id") String sortBy) {

        if (paged) {
            Page<PromotionProposalResponse> proposals = roleService.getPendingPromoteProposalsPaged(page, size, sortBy);
            return ResponseEntity.ok(proposals);
        } else {
            List<PromotionProposalResponse> proposals = roleService.getPendingPromoteProposals();
            return ResponseEntity.ok(proposals);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de promoción", description = "Obtiene los datos de una solicitud de promoción por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Datos devueltos"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content)
    })
    public ResponseEntity<PromotionProposalResponse> getPromoteProposalById(
            @Parameter(description = "ID de la propuesta") @PathVariable @Positive Long id) {
        return ResponseEntity.ok(roleService.getPromoteProposalById(id));
    }

    @PostMapping("/demote/{id}")
    @Operation(summary = "Proponer expulsión", description = "Crea una propuesta de expulsión de un MASTER (solo MASTERs pueden ejecutar)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Propuesta de expulsión creada"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public ResponseEntity<PromotionProposalResponse> demoteToUser(
            @Parameter(description = "ID del MASTER a expulsar") @PathVariable @Positive Long id) {
        return ResponseEntity.ok(roleService.demoteToUser(id));
    }

}
