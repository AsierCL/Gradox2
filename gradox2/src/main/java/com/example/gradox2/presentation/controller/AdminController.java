package com.example.gradox2.presentation.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.presentation.dto.admin.BannedUserResponse;
import com.example.gradox2.service.interfaces.IUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/admin/users")
@Tag(name = "Administración", description = "Gestión de usuarios por MASTER (baneo y rehabilitación)")
public class AdminController {

    private final IUserService userService;

    public AdminController(IUserService userService) {
        this.userService = userService;
    }

    @PutMapping("/{id}/ban")
    @PreAuthorize("hasRole('MASTER')")
    @Operation(summary = "Banear usuario", description = "Bloquea el acceso de un usuario (solo MASTER)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario baneado"),
        @ApiResponse(responseCode = "403", description = "No autorizado (se requiere MASTER)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public ResponseEntity<String> banUser(
            @Parameter(description = "ID del usuario a banear") @PathVariable @Positive Long id,
            @Parameter(description = "Razón del baneo (opcional)") @RequestParam(required = false) String reason) {
        userService.banUser(id, reason);
        return ResponseEntity.ok("Usuario baneado correctamente");
    }

    @PutMapping("/{id}/unban")
    @PreAuthorize("hasRole('MASTER')")
    @Operation(summary = "Rehabilitar usuario", description = "Desbloquea el acceso de un usuario previamente baneado (solo MASTER)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario rehabilitado"),
        @ApiResponse(responseCode = "403", description = "No autorizado (se requiere MASTER)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public ResponseEntity<String> unbanUser(
            @Parameter(description = "ID del usuario a rehabilitar") @PathVariable @Positive Long id) {
        userService.unbanUser(id);
        return ResponseEntity.ok("Usuario rehabilitado correctamente");
    }

    @GetMapping("/banned")
    @PreAuthorize("hasRole('MASTER')")
    @Operation(summary = "Listar usuarios baneados", description = "Devuelve los usuarios deshabilitados paginados (solo MASTER)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado de baneados"),
        @ApiResponse(responseCode = "403", description = "No autorizado (se requiere MASTER)", content = @Content)
    })
    public ResponseEntity<Page<BannedUserResponse>> getBannedUsers(
            @Parameter(description = "Número de página (base 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.getBannedUsers(page, size));
    }
}
