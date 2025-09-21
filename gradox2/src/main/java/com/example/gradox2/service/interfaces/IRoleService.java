package com.example.gradox2.service.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import com.example.gradox2.presentation.dto.promotionProposal.PromotionProposalResponse;

public interface IRoleService {

    PromotionProposalResponse promoteToMaster();

    List<PromotionProposalResponse> getPendingPromoteProposals();

    Page<PromotionProposalResponse> getPendingPromoteProposalsPaged(int page, int size, String sortBy);

    PromotionProposalResponse demoteToUser(Long id);

    String deleteMyPromoteRequest();

    PromotionProposalResponse getPromoteProposalById(Long id);
}
