package com.example.gradox2.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.presentation.dto.promotionProposal.PromotionProposalResponse;
import com.example.gradox2.service.interfaces.IRoleService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/promoteProposal")
public class RolesController {
    private final IRoleService roleService;

    public RolesController(IRoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/request")
    public ResponseEntity<String> promoteToMaster() {
        return ResponseEntity.ok(roleService.promoteToMaster());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMyPromoteRequest() {
        return ResponseEntity.ok(roleService.deleteMyPromoteRequest());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PromotionProposalResponse>> getPendingPromoteProposals() {
        return ResponseEntity.ok(roleService.getPendingPromoteProposals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionProposalResponse> getPromoteProposalById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getPromoteProposalById(id));
    }

    @PostMapping("/demote/{id}")
    public ResponseEntity<String> demoteToUser(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.demoteToUser(id));
    }

}
