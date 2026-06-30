package com.example.gradox2.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.example.gradox2.presentation.dto.auth.AuthResponse;
import com.example.gradox2.presentation.dto.auth.PasswordResetConfirmRequest;
import com.example.gradox2.presentation.dto.auth.PasswordResetRequest;
import com.example.gradox2.presentation.dto.auth.LoginRequest;
import com.example.gradox2.presentation.dto.auth.RegisterRequest;
import com.example.gradox2.presentation.dto.auth.TokenRequest;
import com.example.gradox2.service.interfaces.IAuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/auth")
@Validated
@Tag(name = "01. Autenticación", description = "Registro, login, verificación, refresh, logout y recuperación de contraseña")
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario con email y contraseña, devuelve JWT y refresh token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login exitoso"),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content),
        @ApiResponse(responseCode = "429", description = "Demasiados intentos, rate limit excedido", content = @Content)
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrarse", description = "Crea una cuenta pendiente de verificación con correo institucional @rai.usc.es")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Registro exitoso, se envía código de verificación"),
        @ApiResponse(responseCode = "409", description = "El usuario o email ya existe", content = @Content)
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/token/refresh")
    @Operation(summary = "Renovar token", description = "Obtiene un nuevo JWT usando el refresh token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token renovado"),
        @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado", content = @Content)
    })
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody TokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Revoca el refresh token para cerrar la sesión")
    @ApiResponse(responseCode = "204", description = "Sesión cerrada")
    public ResponseEntity<Void> logout(@Valid @RequestBody TokenRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/reset-request")
    @Operation(summary = "Solicitar reset de contraseña", description = "Envía un enlace de restablecimiento al email si existe")
    @ApiResponse(responseCode = "200", description = "Si el correo existe, se envía el enlace")
    public ResponseEntity<String> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok("Si el correo existe, se enviará un enlace de restablecimiento");
    }

    @PostMapping("/password/reset")
    @Operation(summary = "Confirmar reset de contraseña", description = "Cambia la contraseña usando el token recibido por email")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contraseña actualizada"),
        @ApiResponse(responseCode = "400", description = "Token inválido o expirado", content = @Content)
    })
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    @GetMapping("/verify")
    @Operation(summary = "Verificar cuenta", description = "Activa la cuenta mediante el token de verificación enviado por email")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cuenta verificada"),
        @ApiResponse(responseCode = "400", description = "Token inválido o expirado", content = @Content)
    })
    public ResponseEntity<String> verifyAccount(
            @Parameter(description = "Token de verificación") @RequestParam("token") @NotBlank String token) {
        boolean verified = authService.verifyToken(token);
        if (verified) {
            return ResponseEntity.ok("Cuenta verificada con éxito");
        } else {
            return ResponseEntity.badRequest().body("Token inválido o expirado");
        }
    }

}
