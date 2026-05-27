package com.example.gradox2.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.presentation.dto.voteConfig.VoteConfigUpdateRequest;
import com.example.gradox2.persistence.entities.GlobalConfig;
import com.example.gradox2.service.interfaces.IGlobalConfigService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/vote-config")
public class VoteConfigController {

    private final IGlobalConfigService voteConfigService;

    public VoteConfigController(IGlobalConfigService voteConfigService) {
        this.voteConfigService = voteConfigService;
    }

    @PutMapping
    @PreAuthorize("hasRole('MASTER')")
        public ResponseEntity<GlobalConfig> updateConfig(@Valid @RequestBody VoteConfigUpdateRequest request) {
    return ResponseEntity.ok(
            voteConfigService.updateConfig(request.getQuorumRequired(), request.getApprovalThreshold(), request.getMaxPendingUploads())
    );
    }

    @GetMapping
    public ResponseEntity<GlobalConfig> getConfig() {
        return ResponseEntity.ok(voteConfigService.getConfig());
    }
}
