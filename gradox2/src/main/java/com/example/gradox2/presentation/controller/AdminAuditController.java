package com.example.gradox2.presentation.controller;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.persistence.entities.enums.ActionType;
import com.example.gradox2.presentation.dto.admin.AuditLogResponse;
import com.example.gradox2.service.interfaces.IAuditService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin/audit")
@Tag(name = "Administración", description = "Registro de auditoría accesible solo para MASTER")
public class AdminAuditController {

    private final IAuditService auditService;

    public AdminAuditController(IAuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasRole('MASTER')")
    @Operation(summary = "Ver registro de auditoría", description = "Lista acciones registradas filtrables por actor, tipo, fechas y paginación (solo MASTER)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Registro de auditoría"),
        @ApiResponse(responseCode = "403", description = "No autorizado (se requiere MASTER)", content = @Content)
    })
    public ResponseEntity<Page<AuditLogResponse>> getAuditLog(
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) ActionType actionType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                auditService.getLogs(actorId, actionType, from, to, page, size));
    }
}