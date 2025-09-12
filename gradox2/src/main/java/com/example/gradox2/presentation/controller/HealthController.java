package com.example.gradox2.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @RequestMapping
    public ResponseEntity<String> checkHealth() {
        return ResponseEntity.ok("OK");
    }
}
