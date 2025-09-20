package com.example.gradox2.utils.mapper;

import com.example.gradox2.persistence.entities.PromotionProposal;
import com.example.gradox2.presentation.dto.promotionProposal.PromotionProposalResponse;

public class PromotionProposerMapper {

    public static final PromotionProposalResponse toPromoteProposalResponse(PromotionProposal promotionProposal) {
        return PromotionProposalResponse.builder()
                .id(promotionProposal.getId())
                .proposer(promotionProposal.getProposer() != null ? promotionProposal.getProposer().getUsername() : null)
                .candidate(promotionProposal.getCandidate() != null ? promotionProposal.getCandidate().getUsername() : null)
                .status(promotionProposal.getStatus() != null ? promotionProposal.getStatus().toString() : null)
                .createdAt(promotionProposal.getCreatedAt())
                .endsAt(promotionProposal.getEndsAt())
                .build();
    }
}
