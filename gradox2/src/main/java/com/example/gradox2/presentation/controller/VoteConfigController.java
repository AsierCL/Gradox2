package com.example.gradox2.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.persistence.entities.VoteConfig;
import com.example.gradox2.presentation.dto.voteConfig.VoteConfigUpdateRequest;
import com.example.gradox2.service.interfaces.IVoteConfigService;

@RestController
@RequestMapping("/vote-config")
public class VoteConfigController {

    private final IVoteConfigService voteConfigService;

    public VoteConfigController(IVoteConfigService voteConfigService) {
        this.voteConfigService = voteConfigService;
    }

    @PutMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<VoteConfig> updateConfig(@RequestBody VoteConfigUpdateRequest request) {
    return ResponseEntity.ok(
            voteConfigService.updateConfig(request.getQuorumRequired(), request.getApprovalThreshold())
    );
    }

    @GetMapping
    public ResponseEntity<VoteConfig> getConfig() {
        return ResponseEntity.ok(voteConfigService.getConfig());
    }
}
