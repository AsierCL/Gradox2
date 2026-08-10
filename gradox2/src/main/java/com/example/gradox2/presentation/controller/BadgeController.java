package com.example.gradox2.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.presentation.dto.badges.BadgeResponse;
import com.example.gradox2.service.interfaces.IBadgeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/badges")
@Tag(name = "Insignias", description = "Catálogo público de insignias y sus iconos")
public class BadgeController {
    private final IBadgeService badgeService;

    public BadgeController(IBadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @GetMapping
    @Operation(summary = "Listar insignias", description = "Lista las insignias del sistema con la URL firmada de su icono (si tiene)")
    @ApiResponse(responseCode = "200", description = "Lista devuelta", content = @Content)
    public ResponseEntity<List<BadgeResponse>> getAllBadges() {
        return ResponseEntity.ok(badgeService.getAllBadges());
    }
}