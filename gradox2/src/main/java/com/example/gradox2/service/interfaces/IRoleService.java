package com.example.gradox2.service.interfaces;

import java.util.List;

import com.example.gradox2.presentation.dto.promotionProposal.PromotionProposalResponse;

public interface IRoleService {

    String promoteToMaster();

    List<PromotionProposalResponse> getPendingPromoteProposals();

    String demoteToUser(Long id);

    String deleteMyPromoteRequest();

    PromotionProposalResponse getPromoteProposalById(Long id);
}
