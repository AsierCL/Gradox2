package com.example.gradox2.service.interfaces;

import com.example.gradox2.presentation.dto.voteConfig.ConfigProposalResponse;

public interface IConfigProposalService {
    ConfigProposalResponse createConfigProposal(Integer quorumRequired, Double approvalThreshold,
            Integer maxPendingUploads, Double masterVoteWeight, Double userVoteWeight);
}