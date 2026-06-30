package com.example.gradox2.utils.mapper;

import com.example.gradox2.persistence.entities.Vote;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.FileVisibility;
import com.example.gradox2.presentation.dto.vote.VoteResponse;
import com.example.gradox2.utils.IdentityVisibility;

public class VoteMapper {
    public static final VoteResponse toVoteResponse(Vote vote, User viewer) {
        return VoteResponse.builder()
                .idVote(vote.getId())
                .proposalId(vote.getProposal().getId())
                .voter(IdentityVisibility.resolveDisplayUsername(vote.getVoter(), viewer, FileVisibility.PUBLIC))
                .inFavor(vote.getInFavor())
                .votedAt(vote.getVotedAt())
                .build();
    }
}
