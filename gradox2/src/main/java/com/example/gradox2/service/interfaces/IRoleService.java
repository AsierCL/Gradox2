package com.example.gradox2.service.interfaces;

import java.util.List;

import com.example.gradox2.presentation.dto.promotionProposal.PromotionProposalResponse;

public interface IRoleService {

    PromotionProposalResponse promoteToMaster();

    List<PromotionProposalResponse> getPendingPromoteProposals();

    PromotionProposalResponse demoteToUser(Long id);

    String deleteMyPromoteRequest();

    PromotionProposalResponse getPromoteProposalById(Long id);
}
