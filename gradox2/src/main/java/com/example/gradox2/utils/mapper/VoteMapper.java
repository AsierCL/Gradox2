package com.example.gradox2.utils.mapper;

import com.example.gradox2.persistence.entities.Vote;
import com.example.gradox2.presentation.dto.vote.VoteResponse;

public class VoteMapper {
    public static final VoteResponse toVoteResponse(Vote vote) {
        return VoteResponse.builder()
                .idVote(vote.getId())
                .proposalId(vote.getProposal().getId())
                .voter(vote.getVoter().getUsername())
                .inFavor(vote.getInFavor())
                .votedAt(vote.getVotedAt())
                .build();
    }
}
