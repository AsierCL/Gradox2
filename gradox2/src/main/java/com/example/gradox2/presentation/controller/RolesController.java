package com.example.gradox2.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.presentation.dto.promotionProposal.PromotionProposalResponse;
import com.example.gradox2.service.interfaces.IRoleService;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;


@RestController
@RequestMapping("/promoteProposal")
@Validated
public class RolesController {
    private final IRoleService roleService;

    public RolesController(IRoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/request")
    public ResponseEntity<PromotionProposalResponse> promoteToMaster() {
        return ResponseEntity.ok(roleService.promoteToMaster());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMyPromoteRequest() {
        return ResponseEntity.ok(roleService.deleteMyPromoteRequest());
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingPromoteProposals(
            @RequestParam(defaultValue = "false") Boolean paged,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        if (paged) {
            Page<PromotionProposalResponse> proposals = roleService.getPendingPromoteProposalsPaged(page, size, sortBy);
            return ResponseEntity.ok(proposals);
        } else {
            List<PromotionProposalResponse> proposals = roleService.getPendingPromoteProposals();
            return ResponseEntity.ok(proposals);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionProposalResponse> getPromoteProposalById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(roleService.getPromoteProposalById(id));
    }

    @PostMapping("/demote/{id}")
    public ResponseEntity<PromotionProposalResponse> demoteToUser(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(roleService.demoteToUser(id));
    }

}
