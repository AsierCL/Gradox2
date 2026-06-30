package com.example.gradox2.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/health")
@Tag(name = "09. Health Check", description = "Endpoint de verificación de estado de la aplicación")
public class HealthController {

    @RequestMapping
    @Operation(summary = "Health check", description = "Responde 200 OK si la aplicación está funcionando correctamente")
    @ApiResponse(responseCode = "200", description = "Aplicación activa")
    public ResponseEntity<String> checkHealth() {
        return ResponseEntity.ok("Service is up and running!");
    }
}
