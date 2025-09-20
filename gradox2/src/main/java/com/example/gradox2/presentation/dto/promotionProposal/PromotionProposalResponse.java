package com.example.gradox2.presentation.dto.promotionProposal;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PromotionProposalResponse {
    private Long id;
    private String proposer;
    private String candidate;
    private String status;
    private Instant createdAt;
    private Instant endsAt;
}
