package com.example.gradox2.utils.mapper;

import java.util.Objects;

import com.example.gradox2.persistence.entities.Vote;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.FileVisibility;
import com.example.gradox2.presentation.dto.vote.VoteResponse;
import com.example.gradox2.utils.IdentityVisibility;

public class VoteMapper {
    public static final VoteResponse toVoteResponse(Vote vote, User viewer) {
        Objects.requireNonNull(vote, "vote must not be null");
        return VoteResponse.builder()
                .idVote(vote.getId())
                .proposalId(vote.getProposal() != null ? vote.getProposal().getId() : null)
                .voter(IdentityVisibility.resolveDisplayUsername(vote.getVoter(), viewer, FileVisibility.PUBLIC))
                .inFavor(vote.getInFavor() != null ? vote.getInFavor() : false)
                .votedAt(vote.getVotedAt())
                .build();
    }
}
