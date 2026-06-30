package com.example.gradox2.presentation.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.example.gradox2.presentation.dto.users.MyProfileResponse;
import com.example.gradox2.presentation.dto.users.PublicProfileResponse;
import com.example.gradox2.presentation.dto.users.UpdateMyProfileRequest;
import com.example.gradox2.service.interfaces.IUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/users")
@Validated
@Tag(name = "02. Usuarios", description = "Perfil propio, perfiles públicos y listado de usuarios")
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Perfil propio", description = "Obtiene el perfil completo del usuario autenticado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil devuelto"),
        @ApiResponse(responseCode = "403", description = "No autenticado", content = @Content)
    })
    public ResponseEntity<MyProfileResponse> getCurrentUser() {
        MyProfileResponse user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    @Operation(summary = "Editar perfil", description = "Actualiza nombre, alias o bio del perfil propio")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil actualizado"),
        @ApiResponse(responseCode = "403", description = "No autenticado", content = @Content)
    })
    public ResponseEntity<MyProfileResponse> updateCurrentUser(
            @Valid @RequestBody UpdateMyProfileRequest updateMyProfileRequest) {
        MyProfileResponse updatedUser = userService.updateCurrentUser(updateMyProfileRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Perfil público", description = "Obtiene el perfil público de un usuario por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil devuelto"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public ResponseEntity<PublicProfileResponse> getUserProfile(
            @Parameter(description = "ID del usuario") @PathVariable @Positive Long id) {
        PublicProfileResponse userProfile = userService.getUserProfile(id);
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/all")
    @Operation(summary = "Listar usuarios", description = "Lista todos los usuarios, con soporte de paginación y ordenación")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista devuelta"),
        @ApiResponse(responseCode = "403", description = "No autenticado", content = @Content)
    })
    public ResponseEntity<?> getUsers(
            @Parameter(description = "Activar paginación") @RequestParam(defaultValue = "false") Boolean paged,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página (máx 100)") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @Parameter(description = "Campo de ordenación (id, username, email, role, reputation)") @RequestParam(defaultValue = "id") String sortBy) {

        if (paged) {
            Page<PublicProfileResponse> users = userService.getUsersPaged(page, size, sortBy);
            return ResponseEntity.ok(users);
        } else {
            List<PublicProfileResponse> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        }
    }
}
