package com.example.gradox2.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.persistence.entities.PromotionProposal;
import com.example.gradox2.service.interfaces.IRoleService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequestMapping("/roles")
public class RolesController {
    private final IRoleService roleService;

    public RolesController(IRoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/promote-request")
    public ResponseEntity<String> promoteToMaster() {
        return ResponseEntity.ok(roleService.promoteToMaster());
    }

    @GetMapping("/promote/pending")
    public ResponseEntity<List<PromotionProposal>> getPendingPromoteProposals() {
        return ResponseEntity.ok(roleService.getPendingPromoteProposals());
    }


}
