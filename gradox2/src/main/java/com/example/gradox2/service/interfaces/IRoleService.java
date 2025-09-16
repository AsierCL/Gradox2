package com.example.gradox2.service.interfaces;

import java.util.List;

import com.example.gradox2.persistence.entities.PromotionProposal;

public interface IRoleService {

    String promoteToMaster();

    List<PromotionProposal> getPendingPromoteProposals();


}
