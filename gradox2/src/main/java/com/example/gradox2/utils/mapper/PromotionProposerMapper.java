package com.example.gradox2.utils.mapper;

import com.example.gradox2.persistence.entities.PromotionProposal;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.FileVisibility;
import com.example.gradox2.presentation.dto.promotionProposal.PromotionProposalResponse;
import com.example.gradox2.utils.IdentityVisibility;

public class PromotionProposerMapper {

    public static final PromotionProposalResponse toPromotionProposalResponse(PromotionProposal promotionProposal, User viewer) {
        return PromotionProposalResponse.builder()
                .id(promotionProposal.getId())
                .proposer(IdentityVisibility.resolveDisplayUsername(promotionProposal.getProposer(), viewer, FileVisibility.PUBLIC))
                .candidate(IdentityVisibility.resolveDisplayUsername(promotionProposal.getCandidate(), viewer, FileVisibility.PUBLIC))
                .status(promotionProposal.getStatus() != null ? promotionProposal.getStatus().toString() : null)
                .createdAt(promotionProposal.getCreatedAt())
                .endsAt(promotionProposal.getEndsAt())
                .build();
    }
}
