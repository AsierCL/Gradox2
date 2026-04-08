package com.example.gradox2.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.service.interfaces.IUserService;

import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/admin/users")
public class AdminController {

    private final IUserService userService;

    public AdminController(IUserService userService) {
        this.userService = userService;
    }

    @PutMapping("/{id}/ban")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<String> banUser(@PathVariable @Positive Long id) {
        userService.banUser(id);
        return ResponseEntity.ok("Usuario baneado correctamente");
    }

    @PutMapping("/{id}/unban")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<String> unbanUser(@PathVariable @Positive Long id) {
        userService.unbanUser(id);
        return ResponseEntity.ok("Usuario rehabilitado correctamente");
    }
}
