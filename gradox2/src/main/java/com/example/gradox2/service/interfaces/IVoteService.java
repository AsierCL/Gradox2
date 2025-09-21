package com.example.gradox2.service.interfaces;

import com.example.gradox2.presentation.dto.vote.VoteResponse;
import com.example.gradox2.presentation.dto.vote.VoteResultResponse;

public interface IVoteService {
    VoteResponse getMyVote(Long id);
    VoteResultResponse getVoteCount(Long id);
    VoteResponse voteProposal(Long proposalId, boolean upvote);
    String retractVote(Long proposalId);
}
